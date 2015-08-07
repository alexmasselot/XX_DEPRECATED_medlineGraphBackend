package ch.twenty.medlineGraph.mongodb

import ch.twenty.medlineGraph.models.Citation

import play.modules.reactivemongo.json.collection.{JSONCollection, _}
import reactivemongo.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import ch.twenty.medlineGraph.models.JsonSerializer._

/**
 * @author Alexandre Masselot.
 */
class MongoDbCitations {
  val databaseName = "medline_graph"
  val collectionName = "citations"

  val driver = new MongoDriver
  val connection:MongoConnection = driver.connection(List("localhost"))

  lazy val database:DefaultDB = connection(databaseName)
  lazy val coll: JSONCollection = database.collection[JSONCollection](collectionName)


  def insert(entries:Seq[Citation]):Future[Int] = {
    val bulkDocs = entries.map(implicitly[coll.ImplicitlyDocumentProducer](_))

    val bulkResult = coll.bulkInsert(ordered = true)(bulkDocs: _*)
    bulkResult.map(_.totalN)
  }
}
