package ch.twenty.medlineGraph.tools

import java.io.{FileWriter, File}

import akka.actor._
import akka.routing.RoundRobinPool
import ch.twenty.medlineGraph.WithPrivateConfig
import ch.twenty.medlineGraph.parsers.{MedlineCitationXMLParser, MedlineXMLLoader}
import play.api.libs.json.Json

import ch.twenty.medlineGraph.models.JsonSerializer._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * @author Alexandre Masselot.
 */


object Go

case class LoadFailed(file: File, e: Throwable)

case class LoadSucceeded(n: Int)

/**
 * takes one file and load it into json file
 * receive =>
 * case File => load the file
 *  - if it worked, send back LoadSucceeded
 */
class MedlineLoaderJsonOneLineOneFileActor extends Actor with ActorLogging {

  def receive = {
    case (fileIn:File, fileOut:File) =>
      val loader = new MedlineXMLLoader(fileIn.getAbsolutePath)
      val (itCitations, itExceptions) = MedlineCitationXMLParser.iteratorsCitationFailures(loader.iteratorCitation)
      val nbErrors = itExceptions.size
      val writer = new FileWriter(fileOut)
      val n = (for {
        cit <- itCitations
      }yield{
        val js = Json.toJson(cit)
        writer.write(Json.stringify(js)+"\n")
        1
      }).sum
      writer.close()
      log.info(s"$n\t$nbErrors\t${fileIn.getName()}")
      sender ! LoadSucceeded(n)
  }
}

/**
 * get all .gz files from a directory and send them individually to a MedlineLoaderOneFileActor
 * receive :
 * case dirname: File =>
 * - list all files with .gz
 * - set the file to be processesd counter nSubmitedTask
 * - send the file to  actual  MedlineLoaderJsonOneLineOneFileActor (well, the router of them)
 * case LoadSucceeded(n) => decrease nSubmitedTask by 1; if zero, shutdown
 */
class MedlineLoaderJsoneOneDirectoryActor extends Actor with ActorLogging {
  val nWorkers = 3
  var iFinishedWorkers = 0
  var nSubmitedFiles = 0

  val router: ActorRef =
    context.actorOf(RoundRobinPool(nWorkers).props(Props[MedlineLoaderJsonOneLineOneFileActor]), "router")

  def receive = {
    case (fromDir: String, toDir:String) =>
      log.info(s"loading all files from $fromDir -> $toDir")
      val dir = new File(fromDir)
      val files = dir.listFiles.filter(_.getName endsWith ".gz")
      nSubmitedFiles =files.size
      val re = """(.*)\..*""".r
      for {fileIn <- files} {
        val outBasename = fileIn match{
          case re(p)=>p+".json"
          case x => x+".json"
        }
        val fileOut=new File(toDir+"/"+outBasename)
        router ! (fileIn, fileOut)
      }
    case LoadSucceeded(n) =>
      nSubmitedFiles= nSubmitedFiles-1
      if(nSubmitedFiles==0){
        context.system.shutdown()
      }
  }
}

object MedlineCitationToJsonSingleLines extends App with WithPrivateConfig {
  val system = ActorSystem("medline-batch-loader")
  val dataDir = new File(config.getString("dir.spark.data")+"/citations")
  dataDir.mkdirs()

  // Create the 'greeter' actor
  val dirLoader = system.actorOf(Props[MedlineLoaderJsonOneLineOneFileActor], "medline-dir-loader")
  dirLoader ! (config.getString("dir.resources.thirdparties") + "/medleasebaseline", dataDir.getAbsolutePath)

}
