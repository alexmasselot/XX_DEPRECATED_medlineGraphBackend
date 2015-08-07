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

  def checkSuccess(text: String, expectedFirstSentence:String, expectedInstitution: String, expectedCity: String, expectedCountry: String) = {
    val aff = AffiliationInfoParser(text)

    aff.firstSentence must beEqualTo(expectedFirstSentence)

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

  "removeNumericOnly" should {
    AffiliationInfoParser.removeNumericOnly("Rohtak, Haryana, 124001, India") must beEqualTo("Rohtak, Haryana, India")
  }

  "apply()" should {
    "Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia." in {
      checkSuccess("Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia.",
        "Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia",
        "Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne",
        "Melbourne",
        "Australia")
    }

    "University of Melbourne, Melbourne, Australia." in {
      checkSuccess("University of Melbourne, Melbourne, Australia.",
        "University of Melbourne, Melbourne, Australia",
        "University of Melbourne",
        "Melbourne",
        "Australia")

    }
    "remove postal code: Department of Production Engineering, PSG College of Technology, Peelamedu, Coimbatore 641004, India." in {
      checkSuccess("Department of Production Engineering, PSG College of Technology, Peelamedu, Coimbatore 641004, India.",
        "Department of Production Engineering, PSG College of Technology, Peelamedu, Coimbatore 641004, India",
        "Department of Production Engineering, PSG College of Technology, Peelamedu",
        "Coimbatore",
        "India"
      )
    }

    "no city and ref: 1University of Sussex, UK." in {
      checkSuccess("1University of Sussex, UK",
        "University of Sussex, UK",
        "University of Sussex",
        "",
        "UK"
      )
    }

    "multiple affiliations: Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China ; 2 Department of Medical Oncology, 3 Department of Cardiothoracic Surgery, Jinling Hospital, Medical School of Nanjing University, Nanjing, China." in {
      checkSuccess("Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China ; 2 Department of Medical Oncology, 3 Department of Cardiothoracic Surgery, Jinling Hospital, Medical School of Nanjing University, Nanjing, China.",
        "Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China",
        "Department of General Surgery, 101 Hospital of People's Liberation Army",
        "Wuxi",
        "China"
      )
    }
    "US state: The University of Texas Medical School, Department of Diagnostic and Interventional Imaging, Ultrasonics Laboratory, Houston, TX, USA. Electronic address: AKThittai@iitm.ac.in." in {
      checkSuccess("The University of Texas Medical School, Department of Diagnostic and Interventional Imaging, Ultrasonics Laboratory, Houston, TX, USA. Electronic address: AKThittai@iitm.ac.in.",
        "The University of Texas Medical School, Department of Diagnostic and Interventional Imaging, Ultrasonics Laboratory, Houston, TX, USA",
        "The University of Texas Medical School, Department of Diagnostic and Interventional Imaging, Ultrasonics Laboratory",
        "Houston",
        "USA"
      )
    }

    "postal code with dash: Department of Internal Medicine, Teikyo University School of Medicine, 2-11-1 Kaga, Itabashi-ku, Tokyo 173-8606, Japan." in {
      checkSuccess("Department of Internal Medicine, Teikyo University School of Medicine, 2-11-1 Kaga, Itabashi-ku, Tokyo 173-8606, Japan.",
        "Department of Internal Medicine, Teikyo University School of Medicine, 2-11-1 Kaga, Itabashi-ku, Tokyo 173-8606, Japan",
        "Department of Internal Medicine, Teikyo University School of Medicine, 2-11-1 Kaga, Itabashi-ku",
        "Tokyo",
        "Japan"
      )
    }

    "other state: Università degli Studi e-Campus, Via Isimbardi, Novedrate, CO, Italy" in {
      checkSuccess("Università degli Studi e-Campus, Via Isimbardi, Novedrate, CO, Italy",
        "Università degli Studi e-Campus, Via Isimbardi, Novedrate, CO, Italy",
        "Università degli Studi e-Campus, Via Isimbardi",
        "Novedrate",
        "Italy"
      )
    }

    "post code in front: Department of Structural and Geotechnical Engineering, La Sapienza University of Rome, Via Gramsci 53, 00197 Roma, Italy." in {
      checkSuccess("Department of Structural and Geotechnical Engineering, La Sapienza University of Rome, Via Gramsci 53, 00197 Roma, Italy.",
        "Department of Structural and Geotechnical Engineering, La Sapienza University of Rome, Via Gramsci 53, 00197 Roma, Italy",
        "Department of Structural and Geotechnical Engineering, La Sapienza University of Rome, Via Gramsci 53",
        "Roma",
        "Italy"
      )
    }

    "postal code with characters: Biochemistry Department, University of Geneva CH-1211 Geneva, Switzerland and Swiss National Centre for Competence in Research Programme Chemical Biology, CH-1211 Geneva Switzerland." in {
      checkSuccess("Biochemistry Department, University of Geneva CH-1211 Geneva, Switzerland and Swiss National Centre for Competence in Research Programme Chemical Biology, CH-1211 Geneva, Switzerland.",
        "Biochemistry Department, University of Geneva CH-1211 Geneva, Switzerland and Swiss National Centre for Competence in Research Programme Chemical Biology, CH-1211 Geneva, Switzerland",
        "Biochemistry Department, University of Geneva CH-1211 Geneva, Switzerland and Swiss National Centre for Competence in Research Programme Chemical Biology",
        "Geneva",
        "Switzerland"
      )
    }

    "trim: UPMC, Université Paris 6 , Paris , France ; AP-HP, Hôpital Pitié-Salpêtrière, Centre de référence national pour le Lupus Systémique et le syndrome des Antiphospholipides, Service de Médecine Interne 2, 47-83 Boulevard de l'Hôpital , Paris, Cedex , France." in {
      checkSuccess("UPMC, Université Paris 6 , Paris , France ; AP-HP, Hôpital Pitié-Salpêtrière, Centre de référence national pour le Lupus Systémique et le syndrome des Antiphospholipides, Service de Médecine Interne 2, 47-83 Boulevard de l'Hôpital , Paris, Cedex , France.",
        "UPMC, Université Paris 6 , Paris , France",
        "UPMC, Université Paris 6",
        "Paris",
        "France"
      )
    }

    "post code hell: Department of Pediatric Surgery, All India Institute of Medical Sciences (AIIMS),New Delhi-110029, India." in {
      checkSuccess("Department of Pediatric Surgery, All India Institute of Medical Sciences (AIIMS),New Delhi-110029, India.",
        "Department of Pediatric Surgery, All India Institute of Medical Sciences (AIIMS),New Delhi-110029, India",
        "Department of Pediatric Surgery, All India Institute of Medical Sciences (AIIMS)",
        "New Delhi",
        "India"
      )
    }
    "post code hell: Department of Pediatrics, All India Institute of Medical Sciences (AIIMS), Bhubaneswar-751019, India." in {
      checkSuccess("Department of Pediatrics, All India Institute of Medical Sciences (AIIMS), Bhubaneswar-751019, India.",
        "Department of Pediatrics, All India Institute of Medical Sciences (AIIMS), Bhubaneswar-751019, India",
        "Department of Pediatrics, All India Institute of Medical Sciences (AIIMS)",
        "Bhubaneswar",
        "India"
      )
    }
    "post code hell: Department of Neonatology, Manipal Hospital, Bangalore- 560017, India." in {
      checkSuccess("Department of Neonatology, Manipal Hospital, Bangalore- 560017, India.",
        "Department of Neonatology, Manipal Hospital, Bangalore- 560017, India",
        "Department of Neonatology, Manipal Hospital",
        "Bangalore",
        "India"
      )
    }
    "post code hell: Department of Community Medicine, Pandit Bhagwat Dayal Sharma Post Graduate Institute of Medical Sciences, Rohtak, Haryana, 124001, India." in {
      checkSuccess("Department of Community Medicine, Pandit Bhagwat Dayal Sharma Post Graduate Institute of Medical Sciences, Rohtak, Haryana, 124001, India.",
        "Department of Community Medicine, Pandit Bhagwat Dayal Sharma Post Graduate Institute of Medical Sciences, Rohtak, Haryana, 124001, India",
        "Department of Community Medicine, Pandit Bhagwat Dayal Sharma Post Graduate Institute of Medical Sciences, Rohtak",
        "Haryana",
        "India"
      )
    }
    "post code hell: Department of Radiation Oncology, Vanderbilt University Medical Center, Nashville, TN 37232, USA" in {
      checkSuccess("Department of Radiation Oncology, Vanderbilt University Medical Center, Nashville, TN 37232, USA",
        "Department of Radiation Oncology, Vanderbilt University Medical Center, Nashville, TN 37232, USA",
        "Department of Radiation Oncology, Vanderbilt University Medical Center",
        "Nashville",
        "USA"
      )
    }
    "country change: Division of Cardiothoracic Surgery, Department of Surgery, The Li Ka Shing Faculty of Medicine, The University of Hong Kong, Queen Mary Hospital, Hong Kong 999077, China." in {
      checkSuccess("Division of Cardiothoracic Surgery, Department of Surgery, The Li Ka Shing Faculty of Medicine, The University of Hong Kong, Queen Mary Hospital, Hong Kong 999077, China.",
        "Division of Cardiothoracic Surgery, Department of Surgery, The Li Ka Shing Faculty of Medicine, The University of Hong Kong, Queen Mary Hospital, Hong Kong 999077, China",
        "Division of Cardiothoracic Surgery, Department of Surgery, The Li Ka Shing Faculty of Medicine, The University of Hong Kong, Queen Mary Hospital",
        "Hong Kong",
        "China"
      )
    }

    "with email: Department of Orthopaedics, University of California, 9500 Gilman Drive, La Jolla, San Diego, CA 92093-0412, United States. Electronic address: klpsung@eng.ucsd.edu." in {
      checkSuccess("Department of Orthopaedics, University of California, 9500 Gilman Drive, La Jolla, San Diego, CA 92093-0412, United States. Electronic address: klpsung@eng.ucsd.edu.",
        "Department of Orthopaedics, University of California, 9500 Gilman Drive, La Jolla, San Diego, CA 92093-0412, United States",
        "Department of Orthopaedics, University of California, 9500 Gilman Drive, La Jolla, San Diego",
        "CA",
        "United States"
      )
    }

//    "dot in the middle: Department of Nephrology, Post Graduate Institute of Medical Education and Research, Dr. Ram Manohar Lohia Hospital, New Delhi, India." in {
//      checkSuccess("Department of Nephrology, Post Graduate Institute of Medical Education and Research, Dr. Ram Manohar Lohia Hospital, New Delhi, India.",
//        "Department of Nephrology, Post Graduate Institute of Medical Education and Research, Dr. Ram Manohar Lohia Hospital, New Delhi, India",
//        "Department of Nephrology, Post Graduate Institute of Medical Education and Research, Dr. Ram Manohar Lohia Hospital",
//        "New Delhi",
//        "India"
//      )
//    }


    //  ":" in {
    //      checkSuccess("",
    //        "",
    //        "",
    //        ""
    //      )
    //    }

  }
}
