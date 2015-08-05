package ch.twenty.medlineGraph.parsers

import ch.twenty.medlineGraph.models._
import org.specs2.mutable._

/**
  * @author Alexandre Masselot.
  */
class MedlineXMLLoaderSpecs extends Specification {
  val fnamePubmedFred = "test/resources/pubmed_result_fred.xml"
  val fnameMedlineRnd = "test/resources/medline_rnd.xml"

   "pubmed_fred" should {
     "count" in {
       val loader = new MedlineXMLLoader(fnamePubmedFred)
       loader.iteratorCitation.size must beEqualTo(176)
     }

   }

  "medline_rnd" should {
    "count" in {
      val loader = new MedlineXMLLoader(fnameMedlineRnd)
      loader.iteratorCitation.size must beEqualTo(8)
    }

  }
 }
