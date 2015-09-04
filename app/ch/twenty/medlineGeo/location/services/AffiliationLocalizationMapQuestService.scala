package ch.twenty.medlineGeo.location.services

import java.io.{FileWriter, File}

import ch.twenty.medlineGeo.WithPrivateConfig
import ch.twenty.medlineGeo.location._
import ch.twenty.medlineGeo.models.{Country, City, AffiliationInfo}
import ch.twenty.medlineGeo.parsers.CannotParseAffiliationInfo
import com.ning.http.client.AsyncHttpClientConfig
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}

import scala.concurrent.Await
import scala.util.{Success, Failure, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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

case class MapQuestNoAnswerException(str: String) extends Exception(str)

case class UseBatchLocationException() extends Exception

/**
 *
 * @param serverKey privsate key for the server
 * @param backupDirname where to store all mapquest request
 */
object AffiliationLocalizationMapQuestService extends AffiliationLocalizationService with WithPrivateConfig {
  val isBulkOnly = true

  lazy val serverKey = config.getString("mapquest.api.key");

  val backupDir = new File(config.getString("dir.resources.mapquest.backup"))
  backupDir.mkdirs()

  val clientConfig = new NingAsyncHttpClientConfigBuilder().build
  val builder = new AsyncHttpClientConfig.Builder(clientConfig)
  val client = new NingWSClient(builder.build)

  lazy val urlApi = s"http://www.mapquestapi.com/geocoding/v1/batch?key=$serverKey"

  val batchSize = 100

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
  override def locate(affiliationInfos: Iterable[AffiliationInfo]): Iterable[Try[Location]] = {
    val x = (for {
      slice <- affiliationInfos.sliding(batchSize, batchSize)
    } yield {
        val sliceAff = slice.toList
        locateOnBatch(sliceAff)
      }).flatten
    x.toIterable
  }

  def locateOnBatch(affiliationInfos: List[AffiliationInfo]): List[Try[Location]] = {
    val params = buildRequest(affiliationInfos)
    Logger.info(s"searching in MapQuest (${affiliationInfos.size})")

    val fut = {
      client.url(urlApi).post(Json.stringify(params)).map({
        resp =>
          println("------------")
          println(resp.body)
          val jsResponse = Json.parse(resp.body).as[JsObject]

          val backupFile = new File(s"${backupDir.getAbsolutePath()}/mapquest-response-${params.hashCode()}.json")
          val writer = new FileWriter(backupFile)
          writer.write(Json.prettyPrint(jsResponse))
          writer.close()

          val mapResp = parseMapQuestBatch(jsResponse)
          affiliationInfos.map(_.firstSentence)
            .map({ key =>
            mapResp.get(key) match {
              case Some(ans) => ans
              case None => Failure(MapQuestNoAnswerException(key))
            }
          })
      })
    } recover({ case e =>
      logger.warn(s"MAPQUEST GLOBAL PARSING ERROR: ${e.getMessage}\n\t"+ e.getStackTrace.toList.mkString("\n\t"))
        Nil
    })

    Await.result(fut, 60 seconds)
  }

  def buildRequest(affiliationInfos: List[AffiliationInfo]): JsObject = {
    Json.obj(
      "locations" -> affiliationInfos
        .map(_.firstSentence)
        .map(s => Map("street" -> s)),
      "options" -> Json.obj("thumbMaps" -> false, "maxResults" -> 10)
    )
  }

  val maxCloseDistance = 10 * 1000


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
        .map(_.get)

      val tryLocation = locations match {
        case Nil => Failure(MapQuestNotFoundException(key))
        case x1 :: x2 :: Nil if LocationDistance.distance(x1.coordinates, x2.coordinates) > maxCloseDistance => Failure(MapQuestAmbivalentLocationException(x1.coordinates, x2.coordinates))
        case x :: xs => Success(x)
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
    val countryIso = getAdminArea(json, "Country").map(CountryInfoIso.apply)
    Success(Location(Some(city), countryIso, loc))
  }
  catch {
    case e: Throwable => Failure(e)
  }


}