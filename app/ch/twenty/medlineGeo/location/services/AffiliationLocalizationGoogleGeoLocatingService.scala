package ch.twenty.medlineGeo.location.services

import java.io.File
import java.util.Date

import ch.twenty.medlineGeo.WithPrivateConfig
import ch.twenty.medlineGeo.location._
import ch.twenty.medlineGeo.models.{Country, City, AffiliationInfo}
import ch.twenty.medlineGeo.parsers.{AffiliationInfoParser, CannotParseAffiliationInfo}
import com.google.maps.{GeoApiContext, GeocodingApi}
import com.google.maps.model.GeocodingResult
import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.json._
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Success, Failure, Try}
import scala.collection.JavaConversions._

/**
 * localization service based on Google geolocating API
 *
 * data provided by GeoName is license under Collective Commons v3. Thanks to them
 * @author Alexandre Masselot.
 */


case class GoogleGeoLocatingMultipleMatchesException(affiliationInfo: AffiliationInfo) extends Exception(affiliationInfo.orig)

case class GoogleGeoLocatingNoMatchException(affiliationInfo: AffiliationInfo) extends Exception(affiliationInfo.orig)

case class GoogleGeoLocatingAmbivalentLocationException(geoCoordinates1: GeoCoordinates, geoCoordinates2: GeoCoordinates) extends Exception(s"$geoCoordinates1 - $geoCoordinates2")

/**
 *
 */
object AffiliationLocalizationGoogleGeoLocatingService extends AffiliationLocalizationService with WithPrivateConfig {
  val isBulkOnly=false
  def maxCloseDistance = 50 * 1000

  //  val backupDir = new File(config.get(""))
  //  backupDir.mkdirs()
  lazy val context = new GeoApiContext().setApiKey(config.getString("google.api.key"));

  def getTimeMillis = new Date().getTime
  var tThrottleLast = 0
  // how much to wait between two request
  val deltaThrottle = 1000/5
  /**
   *
   * @param affiliationInfo
   * @return
   */
  def locate(affiliationInfo: AffiliationInfo): Try[Location] = {
    val t = getTimeMillis
    val deltaLast = t-tThrottleLast
    if(deltaThrottle< deltaThrottle){
      val w = deltaThrottle - deltaThrottle
      Thread.sleep(w)
    }

    val results: List[GeoCoordinates] = GeocodingApi.geocode(context, AffiliationInfoParser.firstSentence(affiliationInfo)).await()
      .toList
      .map(x => GeoCoordinates(x.geometry.location.lat, x.geometry.location.lng))

    results match {
      case Nil => Failure(GoogleGeoLocatingNoMatchException(affiliationInfo))
      case x1 :: x2 :: Nil if LocationDistance.distance(x1, x2) > maxCloseDistance => Failure(GoogleGeoLocatingAmbivalentLocationException(x1, x2))
      case x :: xs => Success(Location(None, None, x))
    }
  }
}

