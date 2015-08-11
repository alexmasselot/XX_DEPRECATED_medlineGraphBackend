package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.WithPrivateConfig
import ch.twenty.medlineGraph.location._
import ch.twenty.medlineGraph.models.{City, Country, AffiliationInfo}
import ch.twenty.medlineGraph.parsers.{AffiliationInfoParser, CannotParseAffiliationInfo}

import scala.util.{Failure, Try}

/**
 * localization service based on GeoName www.geonames.org
 *
 * data provided by GeoName is license under Collective Commons v3. Thanks to them
 * @author Alexandre Masselot.
 */

case class UnavailableCityCountryException(affiliationInfo: AffiliationInfo) extends Exception(affiliationInfo.orig)

/**
 *
 * @param cityFilename the  city repository file name
 * @param coutryFilename the  country repository file name
 * @param alternatenamesFilename the altername directory repository file name
 */
class AffiliationLocalizationGeoNameService(cityFilename: String = "resources/cities15000.txt",
                                            coutryFilename: String = "resources/countryInfo.txt",
                                            alternatenamesFilename: String = "resources/alternateNames.txt"
                                             ) extends AffiliationLocalizationService {
  val isBulkOnly = false

  val alternateNameDirectory = AlternateNameDirectory.load(alternatenamesFilename)
  val countryDir = CountryDirectory.load(coutryFilename, alternateNameDirectory)
  val cityDir = CityDirectory.load(cityFilename, countryDir)

  /**
   * try a list of CityDirectory strategies to elucidate the afilliation info field
   * @param affiliationInfo free text
   * @return
   */
  def locate(affiliationInfo: AffiliationInfo): Try[Location] = {
    AffiliationInfoParser.potentialCityCountries(affiliationInfo)
      .toIterator
      .map(cityLoc => cityDir(cityLoc.city, cityLoc.country))
      .takeWhile({
      case Failure(e: CityCountryIncompatibilityException) =>
        false
      case _ => true
    })
      .find(_.isSuccess)
      .getOrElse(Failure(UnavailableCityCountryException(affiliationInfo)))
  }

  //    {(affiliationInfo.city, affiliationInfo.country) match {
  //    case (Some(city), Some(country)) => cityDir(city, country)
  //    case _ => Failure(UnavailableCityCountryException(affiliationInfo))
  //  }
}

object AffiliationLocalizationGeoNameService extends WithPrivateConfig {
  def default = new AffiliationLocalizationGeoNameService(
    config.getString("dir.resources.thirdparties") + "/geonames/cities15000.txt",
    config.getString("dir.resources.thirdparties") + "/geonames/countryInfo.txt",
    config.getString("dir.resources.thirdparties") + "/geonames/alternateNames.txt"
  )
}
