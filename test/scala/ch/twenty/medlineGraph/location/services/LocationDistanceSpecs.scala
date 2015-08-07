package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location.{GeoCoordinates, Location, LocationSamples}
import ch.twenty.medlineGraph.models._
import org.specs2.mutable._

import scala.io.Source
import scala.util.Try

/**
 * @author Alexandre Masselot.
 */
class LocationDistanceSpecs extends Specification with LocationSamples {

  "LocationDistance" should {


    def checkDistance(lat1: Double, long1: Double, lat2: Double, long2: Double, expectedDistance: Double) = {
      s"|($lat1, $long1), ($lat2, $long2)| ~ $expectedDistance" in {
        LocationDistance.distance(GeoCoordinates(lat1, long1), GeoCoordinates(lat2, long2)) must beCloseTo(expectedDistance, 100)
      }
    }

    checkDistance(0, 0, 0, 0, 0)
    checkDistance(180, 0, -180, 0, 0)
    checkDistance(38.898556, -77.037852, 38.897147, -77.043934, 549)
    checkDistance(38.898556, -77.037852, 38.897147, -35.043934, 3601072)
    checkDistance(-38.898556, -77.037852, 38.897147, -77.043934, 8650487)
    checkDistance(38.897147, -77.043934, -38.898556, -77.037852, 8650487)
  }

}

