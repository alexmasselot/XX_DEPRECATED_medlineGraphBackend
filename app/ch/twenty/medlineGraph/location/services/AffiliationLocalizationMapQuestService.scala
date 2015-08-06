package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location._
import ch.twenty.medlineGraph.models.{Country, City, AffiliationInfo}
import ch.twenty.medlineGraph.parsers.CannotParseAffiliationInfo
import play.api.libs.json._

import scala.util.{Success, Failure, Try}

/**
 * localization service based on MapQuest
 *
 * data provided by GeoName is license under Collective Commons v3. Thanks to them
 * @author Alexandre Masselot.
 */

case class MapQuestServerKey(value: String) extends AnyVal

case class MapQuestCannotParseJsonException(str: String) extends Exception(str)

case class MapQuestAmbivalentLocationException(geoCoordinates1: GeoCoordinates, geoCoordinates2: GeoCoordinates) extends Exception(s"$geoCoordinates1 - $geoCoordinates2")

/**
 *
 * @param serverKey
 */
class AffiliationLocalizationMapQuestService(serverKey: MapQuestServerKey, backupDir: String) extends AffiliationLocalizationService {

  /**
   *
   * @param affiliationInfo
   * @return
   */
  def locate(affiliationInfo: AffiliationInfo): Try[Location] = ???
}

object AffiliationLocalizationMapQuestService {
  def distance(coord1: GeoCoordinates, coord2: GeoCoordinates):Double =  {
    val earthRadius = 6371000
    val dLat = Math.toRadians(coord2.latitude - coord1.latitude)
    val dLng = Math.toRadians(coord2.longitude - coord1.longitude)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(coord1.latitude)) * Math.cos(Math.toRadians(coord2.latitude)) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    earthRadius * c
  }


  def parse(jsonString: String): Try[Location] = try {
    Json.parse(jsonString) match {
      case o: JsObject => parse(o)
      case _ => Failure(MapQuestCannotParseJsonException(s"cannot convert to JsObject $jsonString"))
    }
  }
  catch {
    case e: Throwable => Failure(e)
  }

  val reAdmin = """(adminArea\d+)Type""".r

  def getAdminArea(json: JsObject, key: String): Option[String] = {
    val f: PartialFunction[(String, JsValue), String] = {
      case (reAdmin(k), v: JsString) if v.value == key => k
    }
    json.fields.collect(f).headOption.map({ k =>
      (json \ k).as[String]
    })
  }

  def parse(json: JsObject): Try[Location] = try {

    val loc = GeoCoordinates((json \ "latLng" \ "lat").as[Double], (json \ "latLng" \ "lng").as[Double])
    val city = City(getAdminArea(json, "City").getOrElse("-"))
    val country = Country(getAdminArea(json, "Country").getOrElse("-"))
    Success(Location(city, country, loc))
  }
  catch {
    case e: Throwable => Failure(e)
  }

}