package ch.twenty.medlineGraph.tools

import ch.twenty.medlineGraph.WithPrivateConfig
import ch.twenty.medlineGraph.location.Location
import ch.twenty.medlineGraph.location.services.{AffiliationLocalizationGeoNameService, AffiliationLocalizationGoogleGeoLocatingService, AffiliationLocalizationMapQuestService, AffiliationLocalizationService}
import ch.twenty.medlineGraph.mongodb.{MongoCollectionToSingleLineJson, MongoDbCitations, MongoDbAffiliations}
import ch.twenty.medlineGraph.parsers.AffiliationInfoParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
 * @author Alexandre Masselot.
 */
object MongoCollectionToFile extends App with WithPrivateConfig {

  MongoCollectionToSingleLineJson.streamAll(MongoDbCitations.collection, "/tmp/citations.jsona")
    .onComplete({ _ => System.exit(0) })

}
