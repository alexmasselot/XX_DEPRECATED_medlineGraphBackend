package ch.twenty.medlineGraph.location.countryLocation

import ch.twenty.medlineGraph.location.{CountryInfoIso, GeoCoordinates, LocationSamples}
import org.specs2.mutable.Specification

/**
 * Created by alex on 04/09/15.
 */
class CountryShapesListSpecs extends Specification with LocationSamples {
  "CountryShapesList" should{
    "size" in {
      loadCountryShapes.countryShapes.size must beEqualTo(4)
    }
    "find one " in {
      val oc = loadCountryShapes.findCountry(GeoCoordinates(29.5, -1.5))
      oc must beSome
      oc.get.iso must beEqualTo(CountryInfoIso("AT"))
    }
  }
}
