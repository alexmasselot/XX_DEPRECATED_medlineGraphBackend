package ch.twenty.medlineGraph.location.countryLocation

import java.awt.Polygon

import play.api.libs.json._

/**
 * @author Alexandre Masselot.
 */
object GeoNameShapeDeserializer {
  implicit val readPoint = new Reads[Point] {
    override def reads(json: JsValue): JsResult[Point] = json match {
      case a: JsArray =>
        val x = a(0).as[Double]
        val y = a(1).as[Double]

        JsSuccess(Point(x, y))
      case _ => JsError(s"cannot parse coordinates out out $json")
    }
  }

  implicit val readPolygonDouble = new Reads[PolygonDouble] {
    override def reads(json: JsValue): JsResult[PolygonDouble] = json match {
      case JsArray(as) =>
        val ps = as.toList.map(a => a.as[Point])
        JsSuccess(new PolygonDouble(ps))
      case x => JsError(s"cannot parse as a list of pair of Double $x")
    }
  }

  implicit val readPolygonList = new Reads[MultiPolygonDouble] {
    override def reads(json: JsValue): JsResult[MultiPolygonDouble] = ((json \ "type").as[String]) match {
      case "Polygon" =>
        val mPolygons = MultiPolygonDouble((json \ "coordinates").as[List[PolygonDouble]])
        JsSuccess(mPolygons)
      case "MultiPolygon" =>
        val xs  = (json \ "coordinates").as[List[List[PolygonDouble]]]
        val mPolygons = MultiPolygonDouble(xs.map (_.head))
        JsSuccess(mPolygons)
      case _ =>
        JsError(s"cannot find Polygon ot MultiplePolygon type in $json")
    }
  }
}
