package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location.LocationSamples
import ch.twenty.medlineGraph.models._
import ch.twenty.medlineGraph.parsers.AffiliationInfoParser
import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class AffiliationLocalizationGeoNameServiceSpecs extends Specification with LocationSamples {


  "AffiliationLocalizationGeoNameService" should {

    def service = new AffiliationLocalizationGeoNameService(filenameCities, filenameCountries, filenameAlternateNames)

    """College of Physicians and Surgeons, Columbia University, New York, NY, USA.""" in {
      val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, New York City, NY, USA.").get)
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(City("New York City"))
      loc.country must beEqualTo(Country("-"))
      loc.coordinates.latitude must beEqualTo(40.71427)
      loc.coordinates.longitude must beEqualTo(-74.00597)
    }


    """College of Physicians and Surgeons, Columbia University, Shīnḏanḏ, Lebanon.""" in {
        val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, Shīnḏanḏ, Lebanon").get)
        tLoc must beAFailedTry
    }

    """unparsable""" in {
      val tLoc = service.locate(AffiliationInfoParser("unparsable").get)
      tLoc must beAFailedTry
    }


  }
}
