package ch.twenty.medlineGraph.mongodb

import java.io.{FileWriter, Writer}

import play.api.Logger
import play.api.libs.iteratee.Iteratee
import reactivemongo.api._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import play.api.libs.json.{Json, JsObject}
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * takes a mongoDB colleciton and write it as single line object.
 * So that we can stream all this to spark
 * @author Alexandre Masselot.
 */
object MongoCollectionToSingleLineJson {
  /**
   *
   * @param collection
   * @param filename
   * @return
   */
  def streamAll(collection: JSONCollection, filename: String): Future[Unit] = {
    streamAll(collection, new FileWriter(filename))
  }

  /**
   * stream all object as single line json on the writer
   * @param collection
   * @param writer
   */
  def streamAll(collection: JSONCollection, writer: Writer): Future[Unit] = {
    val projParams = Json.obj("_id" -> false)
    val enumerator = collection
      .find(Json.obj(), projParams)
      .cursor[JsObject](ReadPreference.nearest)
      .enumerate()

    var i = 0
    val iteratee: Iteratee[JsObject, Unit] = Iteratee.foreach { jsObj =>
      writer.write(Json.stringify(jsObj ++ Json.obj("id"->i)) + "\n")
      i=i+1
    }

    enumerator
      .run(iteratee)
      .map({ _ =>
      writer.close()
      Logger.info("streamed")
    })
  }
}
