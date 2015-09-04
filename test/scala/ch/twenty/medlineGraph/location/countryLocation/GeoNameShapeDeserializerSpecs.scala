package ch.twenty.medlineGraph.location.countryLocation

import ch.twenty.medlineGraph.location.countryLocation.GeoNameShapeDeserializer._
import org.specs2.mutable.Specification
import play.api.libs.json.Json


/**
 * @author Alexandre Masselot.
 */
class GeoNameShapeDeserializerSpecs extends Specification {
  "GeoNameShapeDeserializer" should {
    "get Point" in {
      val json = Json.parse("[100.0, -23.45]")
      val p = Json.fromJson[Point](json).asOpt
      p must beEqualTo(Some(Point(100.0, -23.45)))
    }
    "get  Polygon" in {
      val json = Json.parse( """[[30,-2.348],[29.954,-2.331],[29.91,-2.709],[30,-2.348]]""")
      val op = Json.fromJson[PolygonDouble](json).asOpt
      op must beSome
      val p = op.get
      p must beEqualTo(PolygonDouble(Seq(Point(30, -2.348), Point(29.954, -2.331), Point(29.91, -2.709), Point(30, -2.348))))
    }

    "get shape as Polygon" in {
      val json = Json.parse( """{"type":"Polygon","coordinates":[[[30,-2.348],[29.954,-2.331],[29.91,-2.709],[30,-2.348]]]}""")
      val op = Json.fromJson[MultiPolygonDouble](json).asOpt
      op must beSome
      val p = op.get
      p must beEqualTo(MultiPolygonDouble(List(PolygonDouble(Seq(Point(30, -2.348), Point(29.954, -2.331), Point(29.91, -2.709), Point(30, -2.348))))))
    }

    "get shape as MultiPolygon" in {
      val json = Json.parse( """{"type":"MultiPolygon","coordinates":[[[[42.543,-0.394],[42.553,-0.377],[42.322,-0.659]]],[[[43.335,11.448],[43.356,11.467],[42.944,11.002]]]]}""")
      val op = Json.fromJson[MultiPolygonDouble](json).asOpt
      op must beSome
      val p = op.get
      p must beEqualTo(MultiPolygonDouble(List(PolygonDouble(Seq(Point(42.543, -0.394), Point(42.553, -0.377), Point(42.322, -0.659))),
        PolygonDouble(Seq(Point(43.335, 11.448), Point(43.356, 11.467), Point(42.944, 11.002))))))
    }
  }
}
