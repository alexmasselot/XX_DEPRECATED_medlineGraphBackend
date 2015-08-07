package ch.twenty.medlineGraph.location.services

import java.io.File

import ch.twenty.medlineGraph.location._
import ch.twenty.medlineGraph.models.{Country, City, AffiliationInfo}
import ch.twenty.medlineGraph.parsers.CannotParseAffiliationInfo
import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.json._
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}

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

case class MapQuestNotFoundException(str: String) extends Exception(str)

case class UseBatchLocationException() extends Exception

/**
 *
 * @param serverKey privsate key for the server
 * @param backupDirname where to store all mapquest request
 */
class AffiliationLocalizationMapQuestService(serverKey: MapQuestServerKey, backupDirname: String) extends AffiliationLocalizationService {
  val config = new NingAsyncHttpClientConfigBuilder().build
  val builder = new AsyncHttpClientConfig.Builder(config)
  val client = new NingWSClient(builder.build)

  val backupDir = new File(backupDirname)
  backupDir.mkdirs()

  /**
   *
   * @param affiliationInfo
   * @return
   */
  def locate(affiliationInfo: AffiliationInfo): Try[Location] = throw UseBatchLocationException()


  /**
   * We override the default locate list, to group querie by the hundred
   * @param affiliationInfos
   * @return
   */
  override def locate(affiliationInfos: Traversable[AffiliationInfo]): Traversable[Try[Location]] = {
    ???
  }

  def urlApi = s"http://www.mapquestapi.com/geocoding/v1/batch?key=$serverKey"

  def buildRequest(affiliationInfos: Traversable[AffiliationInfo]): Unit ={

  }
}

object AffiliationLocalizationMapQuestService {
  val maxCloseDistance=10*1000


  def parseMapQuestBatch(jsonString: String): Map[String, Try[Location]] =
    Json.parse(jsonString) match {
      case o: JsObject => parseMapQuestBatch(o)
      case _ => throw MapQuestCannotParseJsonException(s"cannot convert to JsObject $jsonString")
    }


  def parseMapQuestBatch(jsObject: JsObject): Map[String, Try[Location]] = {
    val jsRes = (jsObject \ "results").as[List[JsObject]]
    jsRes.map({ x =>
      val key = (x \ "providedLocation" \ "location").as[String]
      val locations = (x \ "locations").as[List[JsObject]]
        .filterNot(j => (j \ "geocodeQuality").as[String] == "COUNTRY")
        .map(j => parseOneLocation(j))
        .filter(_.isSuccess)
      .map (_.get)

      val tryLocation = locations match {
        case Nil => Failure(MapQuestNotFoundException(key))
        case x1::x2::Nil if LocationDistance.distance(x1.coordinates, x2.coordinates)>maxCloseDistance => Failure(MapQuestAmbivalentLocationException(x1.coordinates, x2.coordinates))
        case x::xs => Success(x)
      }
      (key, tryLocation)
    })
    .toMap
  }


  /**
   * parse one location from a mapquest json string
   * @param jsonString json data
   * @return
   */
  def parseOneLocation(jsonString: String): Try[Location] = try {
    Json.parse(jsonString) match {
      case o: JsObject => parseOneLocation(o)
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

  /**
   * parse one location from a mapquest json object
   * @param json json data
   * @return
   */
  def parseOneLocation(json: JsObject): Try[Location] = try {

    val loc = GeoCoordinates((json \ "latLng" \ "lat").as[Double], (json \ "latLng" \ "lng").as[Double])
    val city = City(getAdminArea(json, "City").getOrElse("-"))
    val country = Country(getAdminArea(json, "Country").getOrElse("-"))
    Success(Location(city, country, loc))
  }
  catch {
    case e: Throwable => Failure(e)
  }


}