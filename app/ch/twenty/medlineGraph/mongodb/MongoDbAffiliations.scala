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
   * find affiliation with unresolved location
   * @return a list[JsObject, String] where the string is the affiliation text and the JsObject the object to be modified and turned back into the data base)
   */
  def findUnresolved: Future[Iterator[JsObject]] = {
    val query = Json.obj("resolvedLocation" -> Json.obj("$exists" -> 0))
    println(Json.stringify(query))
    collection.find(query)
      .cursor[JsObject]
      .collect[Iterator]()
  }


  def enumerateUnresolved: Enumerator[JsObject] = {
    val query = Json.obj("resolvedLocation" -> Json.obj("$exists" -> 0))
    collection.find(query)
      .cursor[JsObject]
      .enumerate()
  }

  def processUnresolved(resolverName: String, resolver: ((String) => Try[Location])): Iteratee[JsObject, Unit] =
    Iteratee.foreach { jsObj =>
      val affString = (jsObj \ "affiliationShort").as[String]
      try {
        resolver(affString) match {
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
        }
      } catch {
        case e: Throwable => println(s"MAJOR FAILUrE ${e.getMessage}")
      }
    }


  /**
   * For all item with unresolved location, try to
   * @param resolverName
   * @param resolver
   * @return
   */
  def resolve(resolverName: String, resolver: ((String) => Try[Location])): Future[Unit] = {
    enumerateUnresolved.run(processUnresolved(resolverName, resolver))
  }

  //    val query = Json.obj("resolvedLocation" -> Json.obj("$exists" -> false))
  //    println(Json.stringify(query))
  //    var nTot = 0
  //    var nErr = 0
  //    val x = collection
  //      .find(query)
  //      .cursor[JsObject](ReadPreference.Nearest)
  //      .enumerate()
  //      .apply(Iteratee.foreach { jsObj =>
  //      nTot = nTot + 1
  //      println("")
  //      println(jsObj)
  //      val affString = (jsObj \ "affiliationShort").as[String]
  //      println(affString)
  //      resolver(affString) match {
  //        case Success(loc) =>
  //          println(s"$resolverName\t$loc\$affString")
  //          collection.update(jsObj, Json.obj("resolvedLocation" -> Json.obj("location" -> Json.toJson(loc), "resolverName" -> resolverName)))
  //        case Failure(e) =>
  //          println(s"failed\t\t${e.getMessage}")
  //          nErr = nErr + 1
  //      }
  //    })
  //    x.map({
  //      _ =>
  //        Logger.warn(s"done ($nTot, $nErr)")
  //        (nTot, nErr)
  //
  //    })
  //  }

  //    findUnresolved.map({ itUnresolved =>
  //      Logger.info("back from the future")
  //      var nTot = 0
  //      var nErr = 0
  //      for {
  //        jsObj <- itUnresolved
  //      } {
  //        nTot = nTot + 1
  //        println("jsObj")
  //        val affString = (jsObj \ "affiliationShort").as[String]
  //        resolver(affString) match {
  //          case Success(loc) =>
  //            Logger.info(s"$resolverName\t$loc\$affString")
  //            collection.update(jsObj, Json.obj("resolvedLocation" -> Json.obj("location" -> Json.toJson(loc), "resolverName" -> resolverName)))
  //          case Failure(e) =>
  //            Logger.info(s"failed\t\t${e.getMessage}")
  //            nErr = nErr + 1
  //        }
  //      }
  //      (nTot, nErr)
  //    })
  //  }


  //      for {
  //        slice <- itUnresolved.sliding(nBatch, nBatch)
  //      } yield {
  //        val resolvedObj = slice.map({ jsObj =>
  //          val affString = (jsObj \ "affiliationShort").as[String]
  //          resolver(affString) match {
  //            case Success(loc) => Some(jsObj ++ Json.obj("resolvedLocation" -> Json.obj("location" -> Json.toJson(loc), "resolverName" -> resolverName)))
  //            case Failure(_) => None
  //          }
  //        })
  //        val (lSuccess, lFailure) = resolvedObj.partition(_.isDefined)
  //
  //        //TODO put back lSuccess.map(_.get
  //        collection.update()
  //
  //        (lSuccess.size, lFailure.size)
  //      }
  //      (nTot, nErr)
  //    })
  //}
}
