package ch.twenty.medlineGraph.tools


import java.io.File

import akka.event.Logging
import akka.routing.{Broadcast, RoundRobinPool}
import ch.twenty.medlineGraph.WithPrivateConfig
import ch.twenty.medlineGraph.mongodb.MongoDbCitations
import ch.twenty.medlineGraph.parsers.{MedlineXMLLoader, MedlineCitationXMLParser}
import play.api.Logger

import scala.concurrent._

import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._
import scala.concurrent.duration._
import akka.actor._

import scala.util.{Failure, Success}

/**
 * @author Alexandre Masselot.
 */


object Go

case class LoadFailed(file: File, e: Throwable)

case class LoadSucceeded(n: Int)

/**
 * takes one file and load it into mongo
 */
class MedlineLoaderOneFileActor extends Actor with ActorLogging {

  def receive = {
    case file: File =>
      val loader = new MedlineXMLLoader(file.getAbsolutePath)
      val (itCitations, itExceptions) = MedlineCitationXMLParser.iteratorsCitationFailures(loader.iteratorCitation)
      val nbErrors = itExceptions.size

      MongoDbCitations.insert(itCitations.toSeq).onComplete {
        case Success(n) =>
          log.info(s"$n\t$nbErrors\t${file.getName()}")
          sender ! LoadSucceeded(n)
        case Failure(e) => sender ! LoadFailed(file, e)
      }
  }
}

/**
 * get all .gz files from a directory and send them individually to a MedlineLoaderOneFileActor
 */
class MedlineLoaderOneDirectoryActor extends Actor with ActorLogging {
  val nWorkers = 3
  var iFinishedWorkers = 0
  var nSubmitedTask = 0

  val router: ActorRef =
    context.actorOf(RoundRobinPool(nWorkers).props(Props[MedlineLoaderOneFileActor]), "router")

  def receive = {
    case dirName: String =>
      log.info(s"loading all files from $dirName")
      val dir = new File(dirName)
      val files = dir.listFiles.filter(_.getName endsWith ".gz")
      nSubmitedTask =files.size
      for {file <- files} {
        router ! file
      }
    case LoadSucceeded(n) =>
      nSubmitedTask= nSubmitedTask-1
      if(nSubmitedTask==0){
        context.system.shutdown()
      }
    case LoadFailed(file, e) =>
      log.warning(s"failed\t$file\t${e.getMessage}")
      router ! file
  }

}

object MedlineCitationToMongo extends App with WithPrivateConfig {
  val system = ActorSystem("medline-batch-loader")

  // Create the 'greeter' actor
  val dirLoader = system.actorOf(Props[MedlineLoaderOneDirectoryActor], "medline-dir-loader")
  dirLoader ! config.getString("dir.resources.thirdparties") + "/medleasebaseline"

}
