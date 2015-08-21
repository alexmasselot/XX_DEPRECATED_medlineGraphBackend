package ch.twenty.medlineGraph.countryLocation

import java.awt.Polygon

import play.api.libs.json.{JsResult, JsValue, Reads}

/**
 * @author Alexandre Masselot.
 */
object GeoNameShapeDeserializer extends Reads[List[Polygon]]{
  override def reads(json: JsValue): JsResult[List[Polygon]] = match
}
