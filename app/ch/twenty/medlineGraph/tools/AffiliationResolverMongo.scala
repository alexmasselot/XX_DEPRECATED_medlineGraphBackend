package ch.twenty.medlineGraph.tools

import ch.twenty.medlineGraph.WithPrivateConfig
import ch.twenty.medlineGraph.location.services.{AffiliationLocalizationMapQuestService, AffiliationLocalizationService, AffiliationLocalizationGeoNameService, AffiliationLocalizationGoogleGeoLocatingService}
import ch.twenty.medlineGraph.location.{Location, CityDirectory, CountryDirectory, AlternateNameDirectory}
import ch.twenty.medlineGraph.models.{Country, City}
import ch.twenty.medlineGraph.mongodb.MongoDbAffiliations
import ch.twenty.medlineGraph.parsers.AffiliationInfoParser

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Alexandre Masselot.
 */
object AffiliationResolverMongo extends App with WithPrivateConfig {


  //val resolverName = "google"
  val resolverName = "google"

  def resolverService(rName: String): AffiliationLocalizationService = rName match {
    case "google" => AffiliationLocalizationGoogleGeoLocatingService
    case "geonames" => AffiliationLocalizationGeoNameService.default
    case "mapquest" => AffiliationLocalizationMapQuestService
    case x => throw new IllegalArgumentException(s"no resolver method found for [$x]")
  }

  val service = resolverService(resolverName)

  def resolver: (String) => Try[Location] = { x => service.locate(AffiliationInfoParser(x)) }
  def resolverBulks: (Iterable[String]) => Iterable[Try[Location]] = { itX =>  service.locate(itX.map(x=>AffiliationInfoParser(x)))}

  (if (service.isBulkOnly) {
    MongoDbAffiliations.resolveBulks(resolverName, resolverBulks)
  } else {
    MongoDbAffiliations.resolve(resolverName, resolver)
  })
    .onComplete({
    case Success(_) =>
      logger.info(s"complete")
      System.exit(0)
    case Failure(e) =>
      logger.warn(e.getMessage)
      System.exit(0)
  })

}
