package ch.twenty.medlineGraph.mongodb

import ch.twenty.medlineGraph.models.Citation

import play.modules.reactivemongo.json.collection.{JSONCollection, _}
import reactivemongo.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import ch.twenty.medlineGraph.models.JsonSerializer._

/**
 * @author Alexandre Masselot.
 */
object MongoDbCitations {
  val databaseName = "medline_graph"
  val collectionName = "citations"

  lazy val collection: JSONCollection = {
    val driver = new MongoDriver
    val connection: MongoConnection = driver.connection(List("localhost:27017"), MongoConnectionOptions())

    val database: DefaultDB = connection(databaseName)
    Await.ready(database.collectionNames, 2 seconds)
    database.collection[JSONCollection](collectionName)
  }


  def insert(entries: Seq[Citation]): Future[Int] = {
    val bulkDocs = entries.map(implicitly[collection.ImplicitlyDocumentProducer](_))

    val bulkResult = collection.bulkInsert(ordered = true)(bulkDocs: _*)
    bulkResult.map(_.totalN)
  }
}
