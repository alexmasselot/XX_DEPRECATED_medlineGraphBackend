package ch.twenty.medlineGraph.location

import ch.twenty.medlineGraph.models._
import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class CityDirectorySpecs extends Specification with LocationSamples {


  "CityDirectory" should {
    "check size" in {
      val dir = loadDir
      dir.size must beEqualTo(124)
    }

    "check Rome countryCodeIso" in {
      val oRec = loadDir.get(City("Rome"))
      oRec must beSome
      val lRecs = oRec.get

      lRecs must haveSize(4)
      val rec = lRecs.head
      rec.city must beEqualTo(City("Rome"))
      rec.countryCode must beEqualTo(CountryInfoIso("IT"))

    }

    """("Shīnḏanḏ", "Afghanistan")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Shīnḏanḏ"), Country("Afghanistan"))
      tCityLoc must beSuccessfulTry

      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("Shīnḏanḏ"))
      cityLoc.country must beEqualTo(Country("Afghanistan"))
      cityLoc.coordinates.latitude must beEqualTo(33.30294)
      cityLoc.coordinates.longitude must beEqualTo(62.1474)
    }

    """without accent: ("Shindand", "Afghanistan")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Shindand"), Country("Afghanistan"))
      tCityLoc must beSuccessfulTry

      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("Shīnḏanḏ"))
      cityLoc.country must beEqualTo(Country("Afghanistan"))
      cityLoc.coordinates.latitude must beEqualTo(33.30294)
      cityLoc.coordinates.longitude must beEqualTo(62.1474)
    }

    """miss case: ("shindand", "Afghanistan")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("shindand"), Country("Afghanistan"))
      tCityLoc must beSuccessfulTry

      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("Shīnḏanḏ"))
      cityLoc.country must beEqualTo(Country("Afghanistan"))
      cityLoc.coordinates.latitude must beEqualTo(33.30294)
      cityLoc.coordinates.longitude must beEqualTo(62.1474)
    }

    """uniquivocal city, but missed country: ("shindand", "blablalba")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("shindand"), Country("blabla"))
      tCityLoc must beSuccessfulTry

      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("Shīnḏanḏ"))
      cityLoc.coordinates.latitude must beEqualTo(33.30294)
      cityLoc.coordinates.longitude must beEqualTo(62.1474)
    }


    """equivocal city, missed country, but population is way bigger: ("shindand", "blablalba")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("shindand"), Country("blabla"))
      tCityLoc must beSuccessfulTry

      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("Shīnḏanḏ"))
      cityLoc.coordinates.latitude must beEqualTo(33.30294)
      cityLoc.coordinates.longitude must beEqualTo(62.1474)
    }


    """does not exist in country ("Shīnḏanḏ", "Lebanon")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Shīnḏanḏ"), Country("Lebanon"))
      tCityLoc must beAFailedTry

    }
    """does not exists at all ("Paris", "Lebanon")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Paris"), Country("Lebanon"))
      tCityLoc must beFailedTry.withThrowable[NoCityLocationException]
    }

    """one city exist in multiple countries: ("Tripoli", "Lebanon")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Tripoli"), Country("Lebanon"))
      tCityLoc must beSuccessfulTry

      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("Tripoli"))
      cityLoc.country must beEqualTo(Country("Lebanon"))
      cityLoc.coordinates.latitude must beEqualTo(34.43667)
      cityLoc.coordinates.longitude must beEqualTo(35.84972)
    }

    """multiple matches ("Springfield", "USA")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Springfield"), Country("USA"))
      tCityLoc must beAFailedTry.withThrowable[NoCityLocationException]
    }

    """multiple matches ("Springfield", "United States")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Springfield"), Country("United States"))
      tCityLoc must beFailedTry.withThrowable[NoCityLocationException]
    }

    """one mach USA ("Addison", "United States")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Addison"), Country("United States"))
      tCityLoc must beSuccessfulTry
    }

    """one mach USA with synonym ("Addison", "USA")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Addison"), Country("USA"))
      tCityLoc must beSuccessfulTry
    }

    """New York (no 'City') ("New York", "USA")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("New York"), Country("USA"))
      tCityLoc must beSuccessfulTry
      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("New York City"))
    }


    """New York No WAY ("New York NO WAY", "USA")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("New York NO WAY"), Country("USA"))
      tCityLoc must beFailedTry.withThrowable[NoCityLocationException]

    }

    """country is city ("Queen Mary Hospital", "Hong Kong")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Queen Mary Hospital"), Country("Hong Kong"))
      tCityLoc must beSuccessfulTry

      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("Hong Kong"))
      cityLoc.country must beEqualTo(Country("Hong Kong"))
    }
    """ ("Roma", "Italy")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("Roma"), Country("Italy"))
      tCityLoc must beSuccessfulTry

      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("Rome"))
      cityLoc.country must beEqualTo(Country("Italy"))
    }

    """uniquivocal city labeled as country ("University of Washington", "Seattle")""" in {
      val dir = loadDir
      val tCityLoc = dir(City("University of Washington"), Country("Seattle"))
      tCityLoc must beSuccessfulTry

      val cityLoc = tCityLoc.get
      cityLoc.city must beEqualTo(City("Seattle"))
      cityLoc.country must beEqualTo(Country(""))
    }
    //    """("AF")""" in {
    //      loadDir(CountryInfoIso("AF")) must beEqualTo(CountryInfoRecord(CountryInfoIso("AF"), CountryInfoIso3("AfG"), Country("Afghanistan")))
    //    }
  }


}

