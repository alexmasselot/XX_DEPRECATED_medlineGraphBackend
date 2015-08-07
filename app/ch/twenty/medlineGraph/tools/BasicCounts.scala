package ch.twenty.medlineGraph.tools

import java.io.FileWriter

import ch.twenty.medlineGraph.models.{PubmedId, Citation, AffiliationInfo}
import ch.twenty.medlineGraph.parsers.AffiliationInfoParser
import play.api.Logger

import scala.util.{Failure, Success}


class AffiliationErrorLogger(filename: String) {
  val seen = scala.collection.mutable.Set[String]()

  val writer = new FileWriter(filename)

  def log(pubmedId:PubmedId, aff: AffiliationInfo,exception:Throwable) = {
    val t = AffiliationInfoParser.firstSentence(aff.orig)
    if (!seen.contains(t)) {
      writer.write(s"${pubmedId.value}\t${exception.getClass.getSimpleName}\t${exception.getMessage}\t$t\t${aff.orig}\n")
      seen.add(t)
    }
  }

  def close = writer.close()
}

case class StatCounter(total: Int = 0, parsingError: Int = 0, withAffilitation: Int = 0, locationHist: Map[Int, Int] = Map()) {
  def inc = StatCounter(total + 1, parsingError, withAffilitation, locationHist)

  def incError = StatCounter(total, parsingError + 1, withAffilitation, locationHist)

  def incAffiliation = StatCounter(total, parsingError, withAffilitation + 1, locationHist)

  def incLocationHist(nbLocation: Int) = StatCounter(total, parsingError, withAffilitation, locationHist + ((nbLocation, locationHist.getOrElse(nbLocation, 0) + 1)))

}

/**
 * @author Alexandre Masselot.
 */
object BasicCounts extends App {
  val logger = Logger

  import ch.twenty.medlineGraph.location.{CountryDirectory, AlternateNameDirectory, CityDirectory}
  import ch.twenty.medlineGraph.models.{Country, City}
  import ch.twenty.medlineGraph.parsers.{MedlineCitationXMLParser, MedlineXMLLoader}

  val loader = new MedlineXMLLoader("/Users/amasselo/private/dev/medline-graph/data/medleasebaseline/medline15n0779.xml.gz")
  val (itCitations, itExceptions) = MedlineCitationXMLParser.iteratorsCitationFailures(loader.iteratorCitation)

  val alternateDir = AlternateNameDirectory.load("resources/alternateNames.txt")
  logger.info(s"loaded alternateNames ${alternateDir.size}")
  val countryDir = CountryDirectory.load("resources/countryInfo.txt", alternateDir)
  logger.info(s"loaded countries ${countryDir.size}")
  val cityDir = CityDirectory.load("resources/cities15000.txt", countryDir)
  logger.info(s"loaded cities ${cityDir.size}")

  logger.info(s"number of errors: ${itExceptions.size}")
  val writer = new FileWriter("/tmp/affiliations.tsv")
  val errorLogger = new AffiliationErrorLogger("/tmp/affiliation-error.txt")

  def processCitation(citation: Citation, counter: StatCounter): StatCounter = {
    val affiliations = citation.authors
      .filter(_.affiliation.isDefined)
      .map(_.affiliation.get)
      .distinct

    if (affiliations.size == 0) {
      counter.inc
    } else {
      val locations = affiliations.map({ aff =>
        var city = City(aff.city.map(_.value).getOrElse(no))
        val country = Country(aff.country.map(_.value).getOrElse(no))
        val tLoc = cityDir(city, country)
        if (tLoc.isFailure) {
          errorLogger.log(citation.pubmedId, aff, tLoc.failed.get)
        }
        tLoc
      }).distinct
      val (success, failures) = locations.partition(_.isSuccess)
      if (failures.isEmpty) {
        writer.write( s"""${citation.pubmedId}\t${locations.mkString(",")}\n""")
        counter.incLocationHist(locations.size).incAffiliation.inc
      } else {
        counter.incError.incAffiliation.inc
      }

      //        writer.write(s"${c.pubmedId.value}\t${aff.institution.map(_.value).getOrElse(no)}\t${city.value}\t${country.value}\n")
      //        cityDir(city, country) match {
      //          case Success(loc) => writer.write(s"\t\t${loc.coordinates.latitude}\t${loc.coordinates.longitude}\n")
      //          case Failure(e) => errorLogger.log(aff) //writer.write(s"\t$e\n")
      //        }
      //
      //      })
    }
  }

  val no = "X"
  var tmpStats = StatCounter()
  for {
    c <- itCitations
  } {
    tmpStats=processCitation(c, tmpStats)
  }

  println(tmpStats)

}
