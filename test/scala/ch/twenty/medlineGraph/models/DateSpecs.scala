package scala.ch.twenty.medlineGraph.models

import ch.twenty.medlineGraph.models.{DateParser, Date}
import org.specs2.mutable.Specification

/**
 * @author Alexandre Masselot.
 */
class DateSpecs extends Specification {
  "Date" should {
    "numeric month" in {
      DateParser.parse("1999", "12", "23") must beEqualTo(Date(Some(1999), Some(12), Some(23)))
    }
    "text month" in {
      DateParser.parse("1999", "feb", "23") must beEqualTo(Date(Some(1999), Some(2), Some(23)))
    }
    "text month case" in {
      DateParser.parse("1999", "Feb", "23") must beEqualTo(Date(Some(1999), Some(2), Some(23)))
    }

    "no day" in {
      DateParser.parse("1999", "Feb", "") must beEqualTo(Date(Some(1999), Some(2), None))
    }

    "no month" in {
      DateParser.parse("1999", "", "") must beEqualTo(Date(Some(1999), None, None))
    }
    "no year" in {
      DateParser.parse("", "", "") must beEqualTo(Date(None, None, None))
    }

  }
}
