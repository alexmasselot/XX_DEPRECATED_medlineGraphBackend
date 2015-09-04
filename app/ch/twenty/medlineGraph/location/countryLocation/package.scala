package ch.twenty.medlineGraph.location

import java.awt.Polygon

/**
 * Created by alex on 02/09/15.
 */
package object countryLocation {

  case class Point(x: Double, y: Double)

  val roundMultiplier = 1000

  def toIntPos(x: Double) = Math.round(roundMultiplier * x).toInt

  case class PolygonDouble(val points: Seq[Point]) {
    val jPolygon: Polygon = new Polygon(points.map(p => toIntPos(p.x)).toArray, points.map(p => toIntPos(p.y)).toArray, points.size)
    def contains(coords:GeoCoordinates):Boolean =  {
      val x = toIntPos(coords.latitude)
      val y = toIntPos(coords.longitude)
      jPolygon.contains(x, y)
    }
  }

  case class MultiPolygonDouble(val polygons: Seq[PolygonDouble]){
    def contains(coords:GeoCoordinates):Boolean = {
      polygons.exists(_.contains(coords))
    }
  }


  case class CountryShape(val country:CountryInfoRecord, polygons:MultiPolygonDouble)
  {
    def contains(coords:GeoCoordinates):Boolean = polygons.contains(coords)
  }

}
