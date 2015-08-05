package ch.twenty.medlineGraph.parsers

import ch.twenty.medlineGraph.models._
import org.specs2.mutable._

import scala.util.Success

/**
 * @author Alexandre Masselot.
 */
class AffiliationInfoParserSpecs extends Specification {
  "firstSentence()" should {
    "ha haha." in {
      AffiliationInfoParser.firstSentence("ha haha.") must beEqualTo("ha haha")
    }
    "ha haha" in {
      AffiliationInfoParser.firstSentence("ha haha") must beEqualTo("ha haha")
    }
    "ha haha. paf le chien." in {
      AffiliationInfoParser.firstSentence("ha haha. paf le chien.") must beEqualTo("ha haha")
    }
  }

  def checkSuccess(text: String, expectedInstitution: String, expectedCity: String, expectedCountry: String) = {
    val tryAff = AffiliationInfoParser(text)
    tryAff must beASuccessfulTry

    val aff = tryAff.get
    if (expectedInstitution == "") {
      aff.institution must beNone
    } else {
      aff.institution.isDefined must beEqualTo(true)
      aff.institution.get must beEqualTo(Institution(expectedInstitution))
    }
    if (expectedCity == "") {
      aff.city must beNone
    } else {
      aff.city.isDefined must beEqualTo(true)
      aff.city.get must beEqualTo(City(expectedCity))
    }
    if (expectedCountry == "") {
      aff.country must beNone
    } else {
      aff.country.isDefined must beEqualTo(true)
      aff.country.get must beEqualTo(Country(expectedCountry))
    }
  }

  "apply()" should {
    "Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia." in {
      checkSuccess("Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia.",
        "Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne",
        "Melbourne",
        "Australia")
    }

    "University of Melbourne, Melbourne, Australia." in {
      checkSuccess("University of Melbourne, Melbourne, Australia.",
        "University of Melbourne",
        "Melbourne",
        "Australia")

    }
    "remove postal code: Department of Production Engineering, PSG College of Technology, Peelamedu, Coimbatore 641004, India." in {
      checkSuccess("Department of Production Engineering, PSG College of Technology, Peelamedu, Coimbatore 641004, India.",
        "Department of Production Engineering, PSG College of Technology, Peelamedu",
        "Coimbatore",
        "India"
      )
    }

    "no city and ref: 1University of Sussex, UK." in {
      checkSuccess("1University of Sussex, UK.",
        "University of Sussex",
        "",
        "UK"
      )
    }

    "multiple affiliations: Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China ; 2 Department of Medical Oncology, 3 Department of Cardiothoracic Surgery, Jinling Hospital, Medical School of Nanjing University, Nanjing, China." in {
      checkSuccess("Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China ; 2 Department of Medical Oncology, 3 Department of Cardiothoracic Surgery, Jinling Hospital, Medical School of Nanjing University, Nanjing, China.",
        "Department of General Surgery, 101 Hospital of People's Liberation Army",
        "Wuxi",
        "China"
      )
    }
  }
}
