package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location.{GeoCoordinates, Location, LocationSamples}
import ch.twenty.medlineGraph.models._
import ch.twenty.medlineGraph.parsers.AffiliationInfoParser
import org.specs2.mutable._

import scala.io.Source
import scala.util.Try

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
      val tloc = AffiliationLocalizationMapQuestService.parseOneLocation(str)
      tloc must beASuccessfulTry
      val loc = tloc.get
      loc.city must beEqualTo(City("Rohtak"))
      loc.country must beEqualTo(Country("IN"))
      loc.coordinates.latitude must beEqualTo(28.83878)
      loc.coordinates.longitude must beEqualTo(76.627252)
    }
  }

  "parse output count " in {
    val lmap: Map[String, Try[Location]] = sampleLocationMap
    lmap must haveSize(3)

  }
  "parse output cannot find " in {
    val tLoc = sampleLocationMap("Department of Mechanical and Materials Engineering, Faculty of Engineering and Built Environment, University Kebangsaan Malaysia, UKM, 43600 UKM Bangi, Selangor Darul Ehsan, Malaysia")
    tLoc must beAFailedTry
  }

  "parse output ambivalet " in {
    val tLoc = sampleLocationMap("Red Lion")
    tLoc must beAFailedTry
  }

  "parse output gotcha " in {
    val tLoc = sampleLocationMap("Electronic Department, College of Engineering, Diyala University, Iraq")
    tLoc must beASuccessfulTry

    val loc =tLoc.get
    loc must beEqualTo(Location(City("Rohtak"), Country("IN"), GeoCoordinates(28.83878, 76.627252)))
  }

  def sampleLocationMap: Map[String, Try[Location]] = {
    val str = Source.fromFile("test/resources/mapquest-sample.json").getLines().mkString("\n")
    val lmap = AffiliationLocalizationMapQuestService.parseMapQuestBatch(str)
    lmap
  }
}

