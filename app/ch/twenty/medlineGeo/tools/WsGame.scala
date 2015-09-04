package ch.twenty.medlineGeo.tools

import java.io.File

import com.ning.http.client.AsyncHttpClientConfig
import com.typesafe.config.ConfigFactory
import play.api.Play
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient}

import scala.concurrent.Future

import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Alexandre Masselot.
 */
class WsGrabber {
  val config = new NingAsyncHttpClientConfigBuilder().build
  val builder = new AsyncHttpClientConfig.Builder(config)
  val client = new NingWSClient(builder.build)

  def get(url: String): Future[String] = client.url(url).get().map(resp => resp.body)

  def post(url: String, params:JsObject): Future[String] = {
    println(url)
    println(Json.stringify(params))
     client.url(url).post(Json.stringify(params)).map({
       resp =>
         println(s" we have a response $resp")
         resp.body
     })
  }
}

object WsGame extends App {
  def grabber = new WsGrabber

  //grabber.get("http://www.google.com").map(println)

  val json = Json.obj(
    "locations" -> List("Department of Mechanical and Materials Engineering, Faculty of Engineering and Built Environment, University Kebangsaan Malaysia, UKM, 43600 UKM Bangi, Selangor Darul Ehsan, Malaysia",
      "Electronic Department, College of Engineering, Diyala University, Iraq",
      "Red Lion").map(s => Map("street"->s)),
    "options" -> Json.obj("thumbMaps" -> false, "maxResults" -> 10)
  )
  val config = ConfigFactory.parseFile(new File("conf/private.conf"))

  println(config.getString("paf"))


  //grabber.post("http://www.mapquestapi.com/geocoding/v1/batch?key=sFdj3faTK2tCHWtPvLrUUxviPZUE68AR", json).map(println)
}
