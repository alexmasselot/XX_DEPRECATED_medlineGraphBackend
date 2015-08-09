package ch.twenty.medlineGraph.tools

import ch.twenty.medlineGraph.WithPrivateConfig
import ch.twenty.medlineGraph.location.{Location, CityDirectory, CountryDirectory, AlternateNameDirectory}
import ch.twenty.medlineGraph.models.{Country, City}
import ch.twenty.medlineGraph.mongodb.MongoDbAffiliations
import ch.twenty.medlineGraph.parsers.AffiliationInfoParser

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Alexandre Masselot.
 */
object AffiliationResolverMongo extends App with WithPrivateConfig{
  val alternateDir = AlternateNameDirectory.load(config.getString("dir.resources.thirdparties")+"/geonames/alternateNames.txt")
  logger.info(s"loaded alternateNames ${alternateDir.size}")
  val countryDir = CountryDirectory.load(config.getString("dir.resources.thirdparties")+"/geonames/countryInfo.txt", alternateDir)
  logger.info(s"loaded countries ${countryDir.size}")
  val cityDir = CityDirectory.load(config.getString("dir.resources.thirdparties")+"/geonames/cities15000.txt", countryDir)
  logger.info(s"loaded cities ${cityDir.size}")

  def resolver(affString:String):Try[Location] = {
    val aff = AffiliationInfoParser(affString)
    val city = aff.city.getOrElse(City(""))
    val country = aff.country.getOrElse(Country(""))
    cityDir(city, country)
  }
  MongoDbAffiliations.resolve("geonames", resolver).onComplete({
    case Success(_) => logger.info(s"complete")
    case Failure(e)=> logger.warn(e.getMessage())
  })

}
