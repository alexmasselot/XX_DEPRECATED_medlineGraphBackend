package ch.twenty.medlineGraph.parsers

import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class MedlineCitationXMLParserSpecs extends Specification {
  val fnamePubmedFred = "test/resources/pubmed_result_fred.xml"
  val fnameMedlineRnd = "test/resources/medline_rnd.xml"


  "parsing citations" should {
    "go through them" in {
      val loader = new MedlineXMLLoader(fnamePubmedFred)
      val (itCitations, itExceptions)  = MedlineCitationXMLParser.iteratorsCitationFailures(loader.iteratorCitation)
      itExceptions must haveLength(1)
      itCitations must haveLength(175)
    }
  }
}

