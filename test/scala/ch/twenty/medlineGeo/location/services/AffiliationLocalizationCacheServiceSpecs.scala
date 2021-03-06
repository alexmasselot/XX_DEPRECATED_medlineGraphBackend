package ch.twenty.medlineGeo.location.services

import ch.twenty.medlineGeo.location.{CountryInfoIso, GeoCoordinates, Location, LocationSamples}
import ch.twenty.medlineGeo.models._
import ch.twenty.medlineGeo.parsers.AffiliationInfoParser
import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class AffiliationLocalizationCacheServiceSpecs extends Specification with LocationSamples {

  "AffiliationLocalizationCacheService" should {

    val service = new AffiliationLocalizationCacheService

    def myLoc = Location(Some(City("Saint George")), Some(CountryInfoIso("CH")), GeoCoordinates(-120, 90))

    """don't find""" in {
      val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, Saint George, Switzerland."))
      tLoc must beAFailedTry
    }

    """put and find""" in {
      val aff = AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, Saint George, Switzerland.")
      service.put(aff, myLoc)

      val tLoc = service.locate(aff)
      tLoc must beASuccessfulTry
      val loc = tLoc.get
      loc.coordinates.longitude must beEqualTo(90)
    }
  }
}

