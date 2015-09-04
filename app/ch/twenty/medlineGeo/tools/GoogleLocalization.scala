package ch.twenty.medlineGeo.tools

import java.io.File

import ch.twenty.medlineGeo.location.services.AffiliationLocalizationGoogleGeoLocatingService
import ch.twenty.medlineGeo.parsers.AffiliationInfoParser
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

object GoogleLoc extends App {
  val loc = AffiliationLocalizationGoogleGeoLocatingService.locate(AffiliationInfoParser("School of Molecular and Microbial Biosciences G08, The University of Sydney, NSW 2006, Australia."))
  println(loc)
}
