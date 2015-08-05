package ch.twenty.medlineGraph.parsers

import ch.twenty.medlineGraph.models._

import scala.util.{Failure, Success, Try}

case class CannotParseAffiliationInfo(text:String) extends Exception(text)

/**
 * Parses the <AffiliationInfo> tag into and Affiliation structure (institution, city country and so on)
 * @author Alexandre Masselot
 */
object AffiliationInfoParser {
  val reFirstSentenceGetFirstPointOneSentence = """(.*?)\.""".r
  val reFirstSentenceGetFirstPointMultipleSentences = """(.*?)\. .*""".r

  val reMulti = """(.+?) ; .*""".r

  val reAffiliation3 = """\d*(.*), (.*?)(?: \d+)?, (.*)""".r
  val reAffiliation2 = """\d*(.*), (.*)""".r



  def firstAff(text:String) = text match{
    case reMulti(s) => s
    case s => s
  }

  def firstSentence(text:String) =  text match{
      case reFirstSentenceGetFirstPointMultipleSentences(s) => s
      case reFirstSentenceGetFirstPointOneSentence(s) => s
      case s => s
    }


  def apply(text:String):Try[AffiliationInfo] = firstSentence(firstAff(text)) match{
    case reAffiliation3(in, ci, co) => Success(AffiliationInfo(Some(Institution(in)), Some(City(ci)), Some(Country(co))))
    case reAffiliation2(in, co) => Success(AffiliationInfo(Some(Institution(in)), None, Some(Country(co))))
    case _ => Failure(CannotParseAffiliationInfo(text))
  }
}
