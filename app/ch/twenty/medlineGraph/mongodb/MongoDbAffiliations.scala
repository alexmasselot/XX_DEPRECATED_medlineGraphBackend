package ch.twenty.medlineGraph.mongodb

import ch.twenty.medlineGraph.location.Location
import play.api.Logger
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.{Json, JsObject}
import reactivemongo.api.ReadPreference.PrimaryPreferred
import reactivemongo.api._
import play.api.libs.json.Reads._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

import ch.twenty.medlineGraph.models.JsonSerializer._

/**
 * @author Alexandre Masselot.
 */
object MongoDbAffiliations extends MongoDbCommons {
  val collectionName = "affiliations"

  /**
   *
   * find affiliation with unresolved location, where the seolver has not yet been tried
   * @param resolverName something like 'google', 'geonames'. There is no point of trying again a resolver that has already bee attempted
   * @return a list[JsObject, String] where the string is the affiliation text and the JsObject the object to be modified and turned back into the data base)
   */
  def findUnresolved(resolverName: String): Future[Iterator[JsObject]] = {
    val query = Json.obj(
      "resolvedLocation" -> Json.obj("$exists" -> 0),
      "resolverTried" -> Json.obj("$ne" -> resolverName)
    )
    println(Json.stringify(query))
    collection.find(query)
      .cursor[JsObject]
      .collect[Iterator]()
  }

  def cursorUnresolved: Cursor[JsObject] = {
    val query = Json.obj("resolvedLocation" -> Json.obj("$exists" -> 0))
    collection.find(query)
      .cursor[JsObject]
  }

  def enumerateUnresolved: Enumerator[JsObject] = {
    cursorUnresolved.enumerate()
  }

  def enumerateBulksUnresolved: Enumerator[Iterator[JsObject]] = {
    cursorUnresolved.enumerateBulks()
  }

  /**
   * process one json object from mongo, eventually add the match location or update the resolverTried set
   * @param resolverName
   * @param resolver
   * @param jsObj
   * @return
   */
  def processOne(resolverName: String, resolver: ((String) => Try[Location]), jsObj: JsObject): Unit = {
    val affString = (jsObj \ "affiliationShort").as[String]
    try {
      storeAttemptInMongo(jsObj, resolver(affString), resolverName, affString)
    } catch {
      case e: Throwable => println(s"MAJOR FAILUrE ${e.getMessage}")
    }
  }

  /**
   * update object with location if success or add resolverName to resolverTried set
   * @param jsObj
   * @param resolverName
   * @param tLoc
   */
  def storeAttemptInMongo(jsObj: JsObject, tLoc: Try[Location], resolverName: String, affString: String): Any = {
    tLoc match {
      case Success(loc) =>
        println(s"$resolverName\t$loc\t$affString")
        Await.result({
          collection.update(jsObj, Json.obj(
            "$set" -> Json.obj(
              "resolvedLocation" -> Json.obj(
                "location" -> Json.toJson(loc), "resolverName" -> resolverName))
          )
          )
        }, 1 second)
      case Failure(e) =>
        println(s"failed\t\t${e.getMessage}")
        Await.result({
          collection.update(jsObj, Json.obj(
            "$addToSet" -> Json.obj(
              "resolverTried" -> resolverName
            )
          ))
        }, 1 second)
    }
  }

  def processBulksUnresolved(resolverName: String, resolverBulks: (Iterable[String]) => Iterable[Try[Location]]): Iteratee[Iterator[JsObject], Unit] = {
    Iteratee.foreach { it =>
      val nSlice = 300
      for {
        bulk <- it.sliding(nSlice, nSlice)
      } {
        val m = bulk.map(jsObj => ((jsObj \ "affiliationShort").as[String], jsObj)).toMap
        val affiliationStrings = m.keySet.toList
        val tLocations = resolverBulks(affiliationStrings).toList
        for {
          (aff, tLoc) <- affiliationStrings.zip(tLocations)
        } {
          storeAttemptInMongo(m(aff), tLoc, resolverName, aff)
        }
      }
    }
  }

  def processUnresolved(resolverName: String, resolver: ((String) => Try[Location])): Iteratee[JsObject, Unit] =
    Iteratee.foreach { x => processOne(resolverName, resolver, x) }

  /**
   * For all item with unresolved location, try to a location, using the bulk version of the resolver
   * @param resolverName
   * @param resolverBulks
   * @return
   */
  def resolveBulks(resolverName: String, resolverBulks: (Iterable[String]) => Iterable[Try[Location]]): Future[Unit] = {
    enumerateBulksUnresolved.run(processBulksUnresolved(resolverName, resolverBulks))
  }

  /**
   * For all item with unresolved location, try to map them to a location
   * @param resolverName
   * @param resolver
   * @return
   */
  def resolve(resolverName: String, resolver: ((String) => Try[Location])): Future[Unit] = {
    enumerateUnresolved.run(processUnresolved(resolverName, resolver))
  }

}
