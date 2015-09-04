package ch.twenty.medlineGeo.location.countryLocation

import ch.twenty.medlineGeo.location.GeoCoordinates
import org.specs2.mutable.Specification

/**
 * Created by alex on 04/09/15.
 */
class CountryLocationSpecs extends Specification {
  def makePoly(xys: (Double, Double)*) = {
    PolygonDouble(xys.map(p => Point(p._1, p._2)).toList)
  }

  "PolygonDouble" should {
    val p = makePoly((0.1, 0.2), (0.1, 0.5), (0.4, 0.5), (0.1, 0.2))
    val pneg = makePoly((-0.1, -0.2), (-0.1, -0.5), (-0.4, -0.5), (-0.1, -0.2))
    "contains" in {
      p.contains(GeoCoordinates(0.2, 0.4)) must beTrue
    }
    "does not contains" in {
      p.contains(GeoCoordinates(0.6, 0.25)) must beFalse
    }
    "neg contains" in {
      pneg.contains(GeoCoordinates(-0.2, -0.4)) must beTrue
    }
    "neg does not contains" in {
      pneg.contains(GeoCoordinates(-0.6, -0.25)) must beFalse
    }
  }


  "MultiPolygonDouble" should {
    val mp = MultiPolygonDouble(List(makePoly((0.1, 0.2), (0.1, 0.5), (0.4, 0.5), (0.1, 0.2)), makePoly((-0.1, -0.2), (-0.1, -0.5), (-0.4, -0.5), (-0.1, -0.2))))
    "contains" in {
      mp.contains(GeoCoordinates(0.2, 0.4)) must beTrue
      mp.contains(GeoCoordinates(-0.2, -0.4)) must beTrue
    }
    "not contains" in {
      mp.contains(GeoCoordinates(1.2, 0.4)) must beFalse
      mp.contains(GeoCoordinates(-1.2, -0.4)) must beFalse
    }

  }

}
