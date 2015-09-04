package ch.twenty.medlineGraph.location.countryLocation

import ch.twenty.medlineGraph.location.{CountryInfoRecord, GeoCoordinates, GeoNameId, CountryDirectory}
import GeoNameShapeDeserializer._
import play.api.libs.json.Json

import scala.io.Source

/**
 * Created by alex on 03/09/15.
 */
class CountryShapeList(val countryShapes: List[CountryShape]) {
  def findCountry(coords: GeoCoordinates): Option[CountryInfoRecord] = {
    countryShapes.find(_.contains(coords)).map(_.country)
  }
}

object CountryShapeList {
  def load(filename: String, countryDirectory: CountryDirectory): CountryShapeList = {
    val shapes = Source.fromFile(filename)
      .getLines()
      .drop(1)
      .map({
      line =>
        val l = line.split("\t").toList
        val (id, jsText) = (l.head, l(1))
        val json = Json.parse(jsText)
        CountryShape(countryDirectory.findById(GeoNameId(id.toInt)).get, Json.fromJson[MultiPolygonDouble](json).asOpt.get)
    })
      .toList
    new CountryShapeList(shapes)
  }
}
