package ch.twenty.medlineGeo.location

import ch.twenty.medlineGeo.models.Country
import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class CountryDirectorySpecs extends Specification with LocationSamples {

  "CountryDirectory" should {
    "check size" in {
      val dir = loadCountryDir
      dir.size must beEqualTo(55)
    }

    """("AF")""" in {
      loadCountryDir(CountryInfoIso("AF")) must beEqualTo(CountryInfoRecord(GeoNameId(1149361), CountryInfoIso("AF"), CountryInfoIso3("AFG"), Country("Afghanistan")))
    }

    """isSynonmous("AF", "Afghanistan")""" in {
      loadCountryDir.isSynonymous(CountryInfoIso("AF"), Country("Afghanistan")) must beTrue
    }

    """isSynonmous("AF", "Islamic Republic of Afghanistan")""" in {
      loadCountryDir.isSynonymous(CountryInfoIso("AF"), Country("Islamic Republic of Afghanistan")) must beTrue
    }

  }

}

