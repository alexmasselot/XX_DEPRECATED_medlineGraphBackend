package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location.{GeoCoordinates, Location, LocationSamples}
import ch.twenty.medlineGraph.models._
import ch.twenty.medlineGraph.parsers.AffiliationInfoParser
import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class AffiliationLocalizationCacheServiceSpecs extends Specification with LocationSamples {

  "AffiliationLocalizationCacheService" should {

    val service = new AffiliationLocalizationCacheService

    def myLoc = Location(City("Saint George"), Country("Switzerland"), GeoCoordinates(-120, 90))

    """don't find""" in {
      val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, Saint George, Switzerland.").get)
      tLoc must beAFailedTry
    }

    """put and find""" in {
      val aff = AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, Saint George, Switzerland.").get
      service.put(aff, myLoc)

      val tLoc = service.locate(aff)
      tLoc must beASuccessfulTry
      val loc = tLoc.get
      loc.coordinates.longitude must beEqualTo(90)
    }
  }
}

