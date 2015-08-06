package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location.{GeoCoordinates, Location, LocationSamples}
import ch.twenty.medlineGraph.models._
import ch.twenty.medlineGraph.parsers.AffiliationInfoParser
import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class AffiliationLocalizationMapQuestServiceSpecs extends Specification with LocationSamples {

  "AffiliationLocalizationMapQuestService" should {

    val service = new AffiliationLocalizationCacheService

    def myLoc = Location(City("Saint George"), Country("Switzerland"), GeoCoordinates(-120, 90))

    """parseJson""" in {
      val str = """{
                  "street": "NH10",
                  "adminArea6": "",
                  "adminArea6Type": "Neighborhood",
                  "adminArea5": "Rohtak",
                  "adminArea5Type": "City",
                  "adminArea4": "",
                  "adminArea4Type": "County",
                  "adminArea3": "",
                  "adminArea3Type": "State",
                  "adminArea1": "IN",
                  "adminArea1Type": "Country",
                  "postalCode": "124001",
                  "geocodeQualityCode": "B3CCA",
                  "geocodeQuality": "STREET",
                  "dragPoint": false,
                  "sideOfStreet": "N",
                  "linkId": "ttnkp9yq06k8",
                  "unknownInput": "",
                  "type": "s",
                  "latLng": {
                  "lat": 28.83878,
                  "lng": 76.627252
                  },
                  "displayLatLng": {
                  "lat": 28.83878,
                  "lng": 76.627252
                  }
                  }"""
      val tloc = AffiliationLocalizationMapQuestService.parse(str)
      tloc must beASuccessfulTry
      val loc = tloc.get
      loc.city must beEqualTo(City("Rohtak"))
      loc.country must beEqualTo(Country("IN"))
      loc.coordinates.latitude must beEqualTo(28.83878)
      loc.coordinates.longitude must beEqualTo(76.627252)
    }
  }

  def checkDistance(lat1: Double, long1: Double, lat2: Double, long2: Double, expectedDistance: Double) = {
    s"|($lat1, $long1), ($lat2, $long2)| ~ $expectedDistance" in {
      AffiliationLocalizationMapQuestService.distance(GeoCoordinates(lat1, long1), GeoCoordinates(lat2, long2)) must beCloseTo(expectedDistance, 100)
    }
  }

  checkDistance(0, 0, 0, 0, 0)
  checkDistance(180, 0, -180, 0, 0)
  checkDistance(38.898556, -77.037852, 38.897147, -77.043934, 549)
  checkDistance(38.898556, -77.037852, 38.897147, -35.043934, 3601072)
  checkDistance(-38.898556, -77.037852, 38.897147, -77.043934, 8650487)
  checkDistance(38.897147, -77.043934, -38.898556, -77.037852, 8650487)

}

