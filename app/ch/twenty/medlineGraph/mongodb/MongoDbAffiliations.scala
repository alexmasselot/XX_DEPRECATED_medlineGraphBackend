package ch.twenty.medlineGraph.mongodb

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json.collection.{JSONCollection, _}
import reactivemongo.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


/**
 * @author Alexandre Masselot.
 */
object MongoDbAffiliations extends MongoDbCommons{
  val collectionName = "affiliations"

  /**
   * find affiliation with unresolved location
   * @return a list[JsObject, String] where the string is the affiliation text and the JsObject the object to be modified and turned back into the data base)
   */
  def findUnresolved(): Future[List[(JsObject, String)]] = {
???
//    val query = Json.obj("resolvedLocation" -> Json.obj("$exists"->false))
//    collection.find(query)
//      .cursor[JsObject](ReadPreference.Primary)
//      .collect[List]()
//      .map({
//      fut =>
//        fut.map(js => (js, (js \ "affiliationOrig").as[String]))
//    })
  }
}
