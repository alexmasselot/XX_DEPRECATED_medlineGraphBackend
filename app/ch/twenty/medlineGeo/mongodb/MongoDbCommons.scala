package ch.twenty.medlineGeo.mongodb

import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.{DefaultDB, MongoConnectionOptions, MongoConnection, MongoDriver}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.concurrent.Await

/**
 * @author Alexandre Masselot.
 */
trait MongoDbCommons {
  val databaseName = "medline_graph"
  val collectionName:String

  lazy val collection: JSONCollection = {
    val driver = new MongoDriver
    val connection: MongoConnection = driver.connection(List("localhost:27017"), MongoConnectionOptions())

    val database: DefaultDB = connection(databaseName)
    Await.ready(database.collectionNames, 2 seconds)
    database.collection[JSONCollection](collectionName)
  }

}
