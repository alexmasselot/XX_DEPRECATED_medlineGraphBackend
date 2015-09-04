package ch.twenty.medlineGeo.mongodb

import ch.twenty.medlineGeo.models.Citation

import play.modules.reactivemongo.json.collection.{JSONCollection, _}
import reactivemongo.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import ch.twenty.medlineGeo.models.JsonSerializer._

/**
 * @author Alexandre Masselot.
 */
object MongoDbCitations extends MongoDbCommons{
  val collectionName = "citations"

  def insert(entries: Seq[Citation]): Future[Int] = {
    val bulkDocs = entries.map(implicitly[collection.ImplicitlyDocumentProducer](_))

    val bulkResult = collection.bulkInsert(ordered = true)(bulkDocs: _*)
    bulkResult.map(_.totalN)
  }
}
