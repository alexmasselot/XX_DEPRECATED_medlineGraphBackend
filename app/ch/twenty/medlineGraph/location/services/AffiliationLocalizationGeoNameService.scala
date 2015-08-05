package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location.{AlternateNameDirectory, Location, CountryDirectory, CityDirectory}
import ch.twenty.medlineGraph.models.AffiliationInfo
import ch.twenty.medlineGraph.parsers.CannotParseAffiliationInfo

import scala.util.{Failure, Try}

/**
 * localization service based on GeoName www.geonames.org
 *
 * data provided by GeoName is license under Collective Commons v3. Thanks to them
 * @author Alexandre Masselot.
 */

case class UnavailableCityCountryExcpetion(affiliationInfo: AffiliationInfo) extends Exception(affiliationInfo.orig)

/**
 *
 * @param cityFilename
 * @param coutryFilename
 * @param alternatenamesFilename
 */
class AffiliationLocalizationGeoNameService(cityFilename: String = "resources/cities15000.txt", coutryFilename: String = "resources/countryInfo.txt", alternatenamesFilename: String = "resources/alternateNames.txt") {
  val alternateNameDirectory = AlternateNameDirectory.load(alternatenamesFilename)
  val countryDir = CountryDirectory.load(coutryFilename, alternateNameDirectory)
  val cityDir = CityDirectory.load(cityFilename, countryDir)

  /**
   *
   * @param affiliationInfo
   * @return
   */
  def locate(affiliationInfo: AffiliationInfo): Try[Location] = (affiliationInfo.city, affiliationInfo.country) match {
    case (Some(city), Some(country)) => cityDir(city, country)
    case _ => Failure(UnavailableCityCountryExcpetion(affiliationInfo))
  }
}
