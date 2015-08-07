package ch.twenty.medlineGraph.mongodb

import ch.twenty.medlineGraph.location.Location
import play.api.libs.json.{Json, JsObject}
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
   * find affiliation with unresolved location
   * @return a list[JsObject, String] where the string is the affiliation text and the JsObject the object to be modified and turned back into the data base)
   */
  def findUnresolved: Future[Iterator[JsObject]] = {
    val query = Json.obj("resolvedLocation" -> Json.obj("$exists" -> false))
    val x = collection.find(query)
      .cursor[JsObject](ReadPreference.Primary)
      .collect[Iterator]()
    x
  }


  /**
   * For all item with unresolved location, try to
   * @param resolverName
   * @param resolver
   * @return
   */
  def resolve(resolverName: String, resolver: ((String) => Try[Location])): Future[(Int, Int)] = {
    val nBatch = 3000
    findUnresolved.map({ itUnresolved =>
      for {
        slice <- itUnresolved.sliding(nBatch, nBatch)
      } yield {
        val resolvedObj = slice.map({ jsObj =>
          val affString = (jsObj \ "affiliationShort").as[String]
          resolver(affString) match {
            case Success(loc) => Some(jsObj ++ Json.obj("resolvedLocation" -> Json.obj("location" -> Json.toJson(loc), "resolverName" -> resolverName)))
            case Failure(_) => None
          }
        })
        val (lSuccess, lFailure) = resolvedObj.partition(_.isDefined)

        //TODO put back lSuccess.map(_.get

        (lSuccess.size, lFailure.size)
      }
    })
    ???
  }
}
