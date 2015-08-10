package ch.twenty.medlineGraph.parsers

import ch.twenty.medlineGraph.models._

import scala.util.{Failure, Success, Try}

case class CannotParseAffiliationInfo(text: String) extends Exception(text)

/**
 * Parses the <AffiliationInfo> tag into and Affiliation structure (institution, city country and so on)
 * @author Alexandre Masselot
 */
object AffiliationInfoParser {
  val reFirstSentenceGetFirstPointOneSentence = """(.*?)\.""".r
  val reFirstSentenceGetFirstPointMultipleSentences = """(.*?)\. .*""".r

  val reHeadNumber = """^\d+""".r
  val reRemoveEmail = """(.*)\.[^\.]*\s*[\w]+(?:\.\w+)*@(?:\w+\.)+\w+""".r


  //val reMulti = """(.+?) ; .*""".r

  val reAffiliationRef = """\d*(.*),\s?(.+?),\s?(.+)""".r
  val reAffiliation2 = """\d*(.*),\s?(.*)""".r
  val reAffiliationState = """\d*(.*),\s?(.*?, [A-Z]{2}),\s?(.+)""".r

  //  val rePostalCode = """(?:(?:[A-Z]{1,2}\-)?[\d\-]+ )?(.*?)(?: [\d\-]+)?""".r
  //
  //  def removePostalCode(city:String):City = city match {
  //    case rePostalCode(c)=> City(c)
  //    case c => City(c)
  //  }

  def removeNumericOnly(aff: String) = {
    val reNum = """,\s*([A-Z]{2}\s*)?([\s\d]*)?\s*,""".r
    reNum.replaceAllIn(aff, ",")
  }

  //
  //  def firstAff(text: String) = text match {
  //    case reMulti(s) => removeNumericOnly(s)
  //    case s => removeNumericOnly(s)
  //  }

  def firstSentence(text: String): String = {
    val strA = text.split("; ")(0).trim
    val strB = strA match {
      case reFirstSentenceGetFirstPointMultipleSentences(s) => s
      case reFirstSentenceGetFirstPointOneSentence(s) => s
      case s => s
    }

    val strC = reHeadNumber.replaceFirstIn(strB, "")
    strC match {
      case reRemoveEmail(h) => h
      case x => x
    }
  }

  val reSplitComma = """\s*,\s*""".r
  val reRemoveCommaBlock = """\d+""".r

  def potentialCityCountries(affiliationInfo: AffiliationInfo): List[CityLocation] = {
    reSplitComma.split(affiliationInfo.firstSentence)
      .toList
      .filter({ x =>
      x match {
        case reRemoveCommaBlock() => false
        case _ => true
      }
    })
    .reverse
    .take(3)
    .combinations(2)
    .map(x =>x.reverse)
    .map(p => CityLocation(City(cleanName(p(0))), Country(cleanName(p(1)))))
    .toList

//    affiliationInfo.firstSentence match {
//      case reAffiliationState(in, ci, co) => List(CityLocation(City(cleanName(ci)), Country(cleanName(co))))
//      case reAffiliationRef(in, ci, co) => List(CityLocation(City(cleanName(ci)), Country(cleanName(co))))
//      case reAffiliation2(in, co) => List(CityLocation(City(""), Country(cleanName(co))))
//      case _ => Nil
//    }
  }

  def firstSentence(affiliationInfo: AffiliationInfo): String = firstSentence(affiliationInfo.orig)

  def cleanName(name: String): String = {
    val reSuffix = """[\-\s\d]+(\-[A-Z]{1,2})?$""".r
    val rePrefix = """^([A-Z]{1,2})?[\-\s\d]+""".r
    rePrefix.replaceFirstIn(reSuffix.replaceFirstIn(name.trim(), ""), "")
  }


  def apply(text: String): AffiliationInfo = {
    val fs = firstSentence(text)
    AffiliationInfo(text, fs)
    //    removeNumericOnly(fs) match {
    //      case reAffiliationState(in, ci, co) => AffiliationInfo(text, fs, Some(Institution(in.trim)), Some(City(cleanName(ci))), Some(Country(cleanName(co))))
    //      case reAffiliationRef(in, ci, co) => AffiliationInfo(text, fs, Some(Institution(in.trim)), Some(City(cleanName(ci))), Some(Country(cleanName(co))))
    //      case reAffiliation2(in, co) => AffiliationInfo(text, fs, Some(Institution(in.trim)), None, Some(Country(cleanName(co))))
    //      case _ => AffiliationInfo(text, fs, None, None, None)
    //    }
  }
}

