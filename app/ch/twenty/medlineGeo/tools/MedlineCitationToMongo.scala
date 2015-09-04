package ch.twenty.medlineGeo.tools


import java.io.File

import akka.actor._
import akka.routing.RoundRobinPool
import ch.twenty.medlineGeo.WithPrivateConfig
import ch.twenty.medlineGeo.mongodb.MongoDbCitations
import ch.twenty.medlineGeo.parsers.{MedlineCitationXMLParser, MedlineXMLLoader}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * @author Alexandre Masselot.
 */


//object Go
//
//case class LoadFailed(file: File, e: Throwable)
//
//case class LoadSucceeded(n: Int)

/**
 * takes one file and load it into mongo
 * receive =>
 * case File => load the file
 *  - if it worked, send back LoadSucceeded
 *  - if failed, send back LoadFailed with the file
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
 * receive :
 * case dirname: File =>
 * - list all files with .gz
 * - set the file to be processesd counter nSubmitedTask
 * - send the file to  actual mongodb loader MedlineLoaderOneFileActor (well, the router of them)
 * case LoadSucceeded(n) => decrease nSubmitedTask by 1; if zero, shutdown
 * case LoadFailed(file, exc) => resend the failed file (mainly because of mongo timeout
 */
class MedlineLoaderOneDirectoryActor extends Actor with ActorLogging {
  val nWorkers = 3
  var iFinishedWorkers = 0
  var nSubmitedFiles = 0

  val router: ActorRef =
    context.actorOf(RoundRobinPool(nWorkers).props(Props[MedlineLoaderOneFileActor]), "router")

  def receive = {
    case dirName: String =>
      log.info(s"loading all files from $dirName")
      val dir = new File(dirName)
      val files = dir.listFiles.filter(_.getName endsWith ".gz")
      nSubmitedFiles =files.size
      for {file <- files} {
        router ! file
      }
    case LoadSucceeded(n) =>
      nSubmitedFiles= nSubmitedFiles-1
      if(nSubmitedFiles==0){
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
