package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location.{CityCountryIncompatibilityException, LocationSamples}
import ch.twenty.medlineGraph.models._
import ch.twenty.medlineGraph.parsers.AffiliationInfoParser
import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class AffiliationLocalizationGeoNameServiceSpecs extends Specification with LocationSamples {

  "AffiliationLocalizationGeoNameService" should {

    val service = new AffiliationLocalizationGeoNameService(filenameCities, filenameCountries, filenameAlternateNames)

    """College of Physicians and Surgeons, Columbia University, New York, USA.""" in {
      val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, New York City, NY, USA."))
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(City("New York City"))
      loc.country must beEqualTo(Country(""))
      loc.coordinates.latitude must beEqualTo(40.71427)
      loc.coordinates.longitude must beEqualTo(-74.00597)
    }

    """College of Physicians and Surgeons, Columbia University, New York, badaboum, USA.""" in {
      val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, New York City, badaboum, USA."))
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(City("New York City"))
      loc.country must beEqualTo(Country(""))
      loc.coordinates.latitude must beEqualTo(40.71427)
      loc.coordinates.longitude must beEqualTo(-74.00597)
    }

    """College of Physicians and Surgeons, Columbia University, New York, NY, USA.""" in {
      val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, New York City, NY, USA."))
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(City("New York City"))
      loc.country must beEqualTo(Country(""))
      loc.coordinates.latitude must beEqualTo(40.71427)
      loc.coordinates.longitude must beEqualTo(-74.00597)
    }

    """J. N. Adam Memorial Hospital, Perrysburg, N. Y.""" in {
      val tLoc = service.locate(AffiliationInfoParser("J. N. Adam Memorial Hospital, Perrysburg, N. Y."))
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(City("Perrysburg"))
      loc.country must beEqualTo(Country(""))
      loc.coordinates.latitude must beEqualTo(41.557)
      loc.coordinates.longitude must beEqualTo(-83.62716)
    }


    """College of Physicians and Surgeons, Columbia University, Shīnḏanḏ, Lebanon.""" in {
      val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, Shīnḏanḏ, Lebanon"))
      tLoc must beFailedTry.withThrowable[UnavailableCityCountryException]
    }

    """locate a list""" in {
      val affs = List("College of Physicians and Surgeons, Columbia University, Shīnḏanḏ, Lebanon", "College of Physicians and Surgeons, Columbia University, New York City, NY, USA.")
      .map(x => AffiliationInfoParser(x))
      val locations = service.locate(affs)
      locations.map(_.isSuccess) must beEqualTo(List(false, true))
    }
  }
}

