package ch.twenty.medlineGraph.utils

import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient}

import scala.concurrent.Future

import play.api.Play.current
import play.api.libs.ws._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Alexandre Masselot.
 */
class WsGrabber  {
  val config = new NingAsyncHttpClientConfigBuilder().build
  val builder = new AsyncHttpClientConfig.Builder(config)
  val client = new NingWSClient(builder.build)
  def get(url:String):Future[String] = client.url(url).get().map(resp => resp.body)
}

object WsGame extends App{
 def grabber = new WsGrabber

  grabber.get("http://www.google.com").map(println)
}