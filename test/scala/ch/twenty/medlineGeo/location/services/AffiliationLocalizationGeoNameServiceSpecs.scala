package ch.twenty.medlineGeo.location.services

import ch.twenty.medlineGeo.location.{CountryInfoIso, CityCountryIncompatibilityException, LocationSamples}
import ch.twenty.medlineGeo.models._
import ch.twenty.medlineGeo.parsers.AffiliationInfoParser
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
      loc.city must beEqualTo(Some(City("New York City")))
      loc.countryIso must beEqualTo(Some(CountryInfoIso("US")))
      loc.coordinates.latitude must beEqualTo(40.71427)
      loc.coordinates.longitude must beEqualTo(-74.00597)
    }

    """College of Physicians and Surgeons, Columbia University, New York, badaboum, USA.""" in {
      val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, New York City, badaboum, USA."))
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(Some(City("New York City")))
      loc.countryIso must beEqualTo(Some(CountryInfoIso("US")))
      loc.coordinates.latitude must beEqualTo(40.71427)
      loc.coordinates.longitude must beEqualTo(-74.00597)
    }

    """College of Physicians and Surgeons, Columbia University, New York, NY, USA.""" in {
      val tLoc = service.locate(AffiliationInfoParser("College of Physicians and Surgeons, Columbia University, New York City, NY, USA."))
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(Some(City("New York City")))
      loc.countryIso must beEqualTo(Some(CountryInfoIso("US")))
      loc.coordinates.latitude must beEqualTo(40.71427)
      loc.coordinates.longitude must beEqualTo(-74.00597)
    }

    """J. N. Adam Memorial Hospital, Perrysburg, N. Y.""" in {
      val tLoc = service.locate(AffiliationInfoParser("J. N. Adam Memorial Hospital, Perrysburg, N. Y."))
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(Some(City("Perrysburg")))
      loc.countryIso must beEqualTo(Some(CountryInfoIso("US")))
      loc.coordinates.latitude must beEqualTo(41.557)
      loc.coordinates.longitude must beEqualTo(-83.62716)
    }

    """Department of Medicine, University of Washington, Seattle 98195""" in {
      val tLoc = service.locate(AffiliationInfoParser("Department of Medicine, University of Washington, Seattle 98195"))
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(Some(City("Seattle")))
      loc.countryIso must beEqualTo(Some(CountryInfoIso("US")))
      loc.coordinates.latitude must beEqualTo(47.60621)
      loc.coordinates.longitude must beEqualTo(-122.33207)
    }

    """multiple cities with same name, but one population is way higher: Department of Medicine, University of Washington, London""" in {
      val tLoc = service.locate(AffiliationInfoParser("Department of Medicine, University of Washington, London"))
      tLoc must beSuccessfulTry

      val loc = tLoc.get
      loc.city must beEqualTo(Some(City("London")))
      loc.countryIso must beEqualTo(Some(CountryInfoIso("GB")))
      loc.coordinates.latitude must beEqualTo(51.50853)
      loc.coordinates.longitude must beEqualTo(-0.12574)
    }
    """multiple cities with same name, population are no discriminant: Department of Medicine, University of Washington, Lonpaf""" in {
      val tLoc = service.locate(AffiliationInfoParser("Department of Medicine, University of Washington, Lonpaf"))
      tLoc must beAFailedTry
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

