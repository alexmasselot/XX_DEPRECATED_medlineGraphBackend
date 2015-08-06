package ch.twenty.medlineGraph.location

import ch.twenty.medlineGraph.location.AlternateNameDirectory
import ch.twenty.medlineGraph.models.Country
import org.specs2.mutable._

/**
 * @author Alexandre Masselot.
 */
class AltrernateNameDirectorySpecs extends Specification {

  def loadAlternateNameDir = AlternateNameDirectory.load("test/resources/alternateNames-samples.txt")

  "AltrernateNameDirectory" should {
    "check size" in {
      val dir = loadAlternateNameDir
      dir.size must beEqualTo(6)
    }
  }
}

