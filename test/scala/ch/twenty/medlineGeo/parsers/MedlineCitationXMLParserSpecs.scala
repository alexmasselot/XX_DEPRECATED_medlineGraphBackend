package ch.twenty.medlineGeo.parsers

import ch.twenty.medlineGeo.models.{AbstractText, Title}
import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class MedlineCitationXMLParserSpecs extends Specification {
  val fnamePubmedFred = "test/resources/pubmed_result_fred.xml"
  val fnameMedlineRnd = "test/resources/medline_rnd.xml"


  "parsing citations" should {
    "go through pubmed" in {
      val loader = new MedlineXMLLoader(fnamePubmedFred)
      val (itCitations, itExceptions)  = MedlineCitationXMLParser.iteratorsCitationFailures(loader.iteratorCitation)
      itExceptions must haveLength(0)
      itCitations must haveLength(176)
    }
    "go through medline" in {
      val loader = new MedlineXMLLoader(fnameMedlineRnd)
      val (itCitations, itExceptions)  = MedlineCitationXMLParser.iteratorsCitationFailures(loader.iteratorCitation)
      itExceptions must haveLength(0)
      itCitations must haveLength(8)
    }

    "check first" in {
      val loader = new MedlineXMLLoader(fnameMedlineRnd)
      val (itCitations, itExceptions)  = MedlineCitationXMLParser.iteratorsCitationFailures(loader.iteratorCitation)

      val citation = itCitations.take(1).toList.head
      citation.title must beEqualTo(Title("Studies on thermo-elastic heating of horns used in ultrasonic plastic welding."))
      citation.abstractText.value must startingWith("Ultrasonic welding horn is half wavelength section or tool used to focus the ultrasonic vibrations to the components being welded. The horn is designed in such a way that it maximizes the amplitude")

    }
  }
}

