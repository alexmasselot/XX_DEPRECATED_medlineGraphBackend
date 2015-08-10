package ch.twenty.medlineGraph.parsers

import ch.twenty.medlineGraph.models._
import org.specs2.mutable._

import scala.util.Success

/**
 * @author Alexandre Masselot.
 */
class AffiliationInfoParserSpecs extends Specification {

  def checkFirstSentence(comment: String, text: String, expectedFirstSentence: String) = {
    s"firstSentence $comment: $text" in {
      val aff = AffiliationInfoParser(text)

      aff.firstSentence must beEqualTo(expectedFirstSentence)
    }
  }

  def checkCityCountries(text: String, expected: List[(String, String)]) = {
    text in {
      val aff = AffiliationInfoParser(text)

      val l = AffiliationInfoParser.potentialCityCountries(aff)
      l.take(expected.length) must beEqualTo(expected.map(p => CityLocation(City(p._1), Country(p._2))))
    }
  }


  "apply()" should {

    checkFirstSentence("JohnB.",
      "John B. Pierce Laboratory of Hygiene, New Haven.",
      "John B. Pierce Laboratory of Hygiene, New Haven"
    )

    checkFirstSentence("dept.",
      "Dept. of Clinical Radiology, University of Muenster, Germany.",
      "Dept. of Clinical Radiology, University of Muenster, Germany"
    )
    checkFirstSentence("dot and email",
      "St. Georges Healthcare NHS Trust, London, United Kingdom (N.F.). snick@doctors.org.uk.",
      "St. Georges Healthcare NHS Trust, London, United Kingdom (N.F.)"
    )

    checkFirstSentence("",
      "Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia.",
      "Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia"
    )
    checkCityCountries("Centre for Epidemiology and Biostatistics, Melbourne School of Population and Global Health, University of Melbourne, Melbourne, Australia.",
      List(("Melbourne", "Australia"))
    )

    checkFirstSentence("",
      "University of Melbourne, Melbourne, Australia.",
      "University of Melbourne, Melbourne, Australia"
    )
    checkCityCountries("University of Melbourne, Melbourne, Australia.",
      List(("Melbourne", "Australia"))
    )

    checkFirstSentence("",
      "Department of Production Engineering, PSG College of Technology, Peelamedu, Coimbatore 641004, India.",
      "Department of Production Engineering, PSG College of Technology, Peelamedu, Coimbatore 641004, India"
    )

    checkCityCountries("Department of Production Engineering, PSG College of Technology, Peelamedu, Coimbatore 641004, India.",
      List(("Coimbatore", "India"))
    )
    checkFirstSentence("remove ref", "1University of Sussex, UK",
      "University of Sussex, UK"
    )
    checkCityCountries("1University of Sussex, UK",
      List(("University of Sussex", "UK"))
    )
    checkFirstSentence("multiple affiliations",
      "Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China ; 2 Department of Medical Oncology, 3 Department of Cardiothoracic Surgery, Jinling Hospital, Medical School of Nanjing University, Nanjing, China.",
      "Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China"
    )
    checkCityCountries("Department of General Surgery, 101 Hospital of People's Liberation Army, Wuxi 214044, China ; 2 Department of Medical Oncology, 3 Department of Cardiothoracic Surgery, Jinling Hospital, Medical School of Nanjing University, Nanjing, China.",
      List(("Wuxi", "China"))
    )
    checkFirstSentence("US state",
      "The University of Texas Medical School, Department of Diagnostic and Interventional Imaging, Ultrasonics Laboratory, Houston, TX, USA. Electronic address: AKThittai@iitm.ac.in.",
      "The University of Texas Medical School, Department of Diagnostic and Interventional Imaging, Ultrasonics Laboratory, Houston, TX, USA"
    )
    checkCityCountries("The University of Texas Medical School, Department of Diagnostic and Interventional Imaging, Ultrasonics Laboratory, Houston, TX, USA. Electronic address: AKThittai@iitm.ac.in.",
      List(("TX", "USA"), ("Houston", "USA"))
    )

    checkFirstSentence("postal code with dash",
      "Department of Internal Medicine, Teikyo University School of Medicine, 2-11-1 Kaga, Itabashi-ku, Tokyo 173-8606, Japan.",
      "Department of Internal Medicine, Teikyo University School of Medicine, 2-11-1 Kaga, Itabashi-ku, Tokyo 173-8606, Japan"
    )
    checkCityCountries("Department of Internal Medicine, Teikyo University School of Medicine, 2-11-1 Kaga, Itabashi-ku, Tokyo 173-8606, Japan.",
      List(("Tokyo", "Japan"))
    )

    checkFirstSentence("other state",
      "Università degli Studi e-Campus, Via Isimbardi, Novedrate, CO, Italy",
      "Università degli Studi e-Campus, Via Isimbardi, Novedrate, CO, Italy"
    )
    checkCityCountries("Università degli Studi e-Campus, Via Isimbardi, Novedrate, CO, Italy",
      List(("CO", "Italy"), ("Novedrate", "Italy"))
    )
    checkFirstSentence("post code in front",
      "Department of Structural and Geotechnical Engineering, La Sapienza University of Rome, Via Gramsci 53, 00197 Roma, Italy.",
      "Department of Structural and Geotechnical Engineering, La Sapienza University of Rome, Via Gramsci 53, 00197 Roma, Italy"
    )

    checkCityCountries("Department of Structural and Geotechnical Engineering, La Sapienza University of Rome, Via Gramsci 53, 00197 Roma, Italy.",
      List(("Roma", "Italy"))
    )

    checkFirstSentence("postal code with characters",
      "Biochemistry Department, University of Geneva CH-1211 Geneva, Switzerland and Swiss National Centre for Competence in Research Programme Chemical Biology, CH-1211 Geneva, Switzerland.",
      "Biochemistry Department, University of Geneva CH-1211 Geneva, Switzerland and Swiss National Centre for Competence in Research Programme Chemical Biology, CH-1211 Geneva, Switzerland"
    )

    checkCityCountries("Biochemistry Department, University of Geneva CH-1211 Geneva, Switzerland and Swiss National Centre for Competence in Research Programme Chemical Biology, CH-1211 Geneva, Switzerland.",
      List(("Geneva", "Switzerland"))
    )

    checkFirstSentence("trim",
      "UPMC, Université Paris 6 , Paris , France ; AP-HP, Hôpital Pitié-Salpêtrière, Centre de référence national pour le Lupus Systémique et le syndrome des Antiphospholipides, Service de Médecine Interne 2, 47-83 Boulevard de l'Hôpital , Paris, Cedex , France.",
      "UPMC, Université Paris 6 , Paris , France"
    )
    checkCityCountries("UPMC, Université Paris 6 , Paris , France ; AP-HP, Hôpital Pitié-Salpêtrière, Centre de référence national pour le Lupus Systémique et le syndrome des Antiphospholipides, Service de Médecine Interne 2, 47-83 Boulevard de l'Hôpital , Paris, Cedex , France.",
      List(("Paris", "France"))
    )

    checkFirstSentence("post code hell",
      "Department of Pediatric Surgery, All India Institute of Medical Sciences (AIIMS),New Delhi-110029, India.",
      "Department of Pediatric Surgery, All India Institute of Medical Sciences (AIIMS),New Delhi-110029, India"
    )
    checkCityCountries("Department of Pediatric Surgery, All India Institute of Medical Sciences (AIIMS),New Delhi-110029, India.",
      List(("New Delhi", "India"))
    )
    checkFirstSentence("post code hell",
      "Department of Pediatrics, All India Institute of Medical Sciences (AIIMS), Bhubaneswar-751019, India.",
      "Department of Pediatrics, All India Institute of Medical Sciences (AIIMS), Bhubaneswar-751019, India"
    )
    checkCityCountries("Department of Pediatrics, All India Institute of Medical Sciences (AIIMS), Bhubaneswar-751019, India.",
      List(("Bhubaneswar", "India"))
    )

    checkFirstSentence("post code hell", "Department of Neonatology, Manipal Hospital, Bangalore- 560017, India.",
      "Department of Neonatology, Manipal Hospital, Bangalore- 560017, India"
    )
    checkCityCountries("Department of Neonatology, Manipal Hospital, Bangalore- 560017, India.",
      List(("Bangalore", "India"))
    )
    checkFirstSentence("post code hell",
      "Department of Community Medicine, Pandit Bhagwat Dayal Sharma Post Graduate Institute of Medical Sciences, Rohtak, Haryana, 124001, India.",
      "Department of Community Medicine, Pandit Bhagwat Dayal Sharma Post Graduate Institute of Medical Sciences, Rohtak, Haryana, 124001, India"
    )

    checkCityCountries("Department of Community Medicine, Pandit Bhagwat Dayal Sharma Post Graduate Institute of Medical Sciences, Rohtak, Haryana, 124001, India.",
      List(("Haryana", "India"))
    )

    checkFirstSentence("post code hell",
      "Department of Radiation Oncology, Vanderbilt University Medical Center, Nashville, TN 37232, USA",
      "Department of Radiation Oncology, Vanderbilt University Medical Center, Nashville, TN 37232, USA"
    )
    checkCityCountries("Department of Radiation Oncology, Vanderbilt University Medical Center, Nashville, TN 37232, USA",
      List(("TN", "USA"), ("Nashville", "USA"))
    )

    checkFirstSentence("country change",
      "Division of Cardiothoracic Surgery, Department of Surgery, The Li Ka Shing Faculty of Medicine, The University of Hong Kong, Queen Mary Hospital, Hong Kong 999077, China.",
      "Division of Cardiothoracic Surgery, Department of Surgery, The Li Ka Shing Faculty of Medicine, The University of Hong Kong, Queen Mary Hospital, Hong Kong 999077, China"
    )


    checkCityCountries("Division of Cardiothoracic Surgery, Department of Surgery, The Li Ka Shing Faculty of Medicine, The University of Hong Kong, Queen Mary Hospital, Hong Kong 999077, China.",
      List(("Hong Kong", "China"))
    )

    checkFirstSentence("with email",
      "Department of Orthopaedics, University of California, 9500 Gilman Drive, La Jolla, San Diego, CA 92093-0412, United States. Electronic address: klpsung@eng.ucsd.edu.",
      "Department of Orthopaedics, University of California, 9500 Gilman Drive, La Jolla, San Diego, CA 92093-0412, United States"
    )
    checkCityCountries("Department of Orthopaedics, University of California, 9500 Gilman Drive, La Jolla, San Diego, CA 92093-0412, United States. Electronic address: klpsung@eng.ucsd.edu.",
      List(("CA", "United States"))
    )

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
