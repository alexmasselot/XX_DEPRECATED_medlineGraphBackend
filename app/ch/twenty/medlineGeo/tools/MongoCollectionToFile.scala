package ch.twenty.medlineGeo.tools

import java.io.File

import ch.twenty.medlineGeo.WithPrivateConfig
import ch.twenty.medlineGeo.location.Location
import ch.twenty.medlineGeo.location.services.{AffiliationLocalizationGeoNameService, AffiliationLocalizationGoogleGeoLocatingService, AffiliationLocalizationMapQuestService, AffiliationLocalizationService}
import ch.twenty.medlineGeo.mongodb.{MongoCollectionToSingleLineJson, MongoDbCitations, MongoDbAffiliations}
import ch.twenty.medlineGeo.parsers.AffiliationInfoParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
 * @author Alexandre Masselot.
 */
object MongoCollectionToFile extends App with WithPrivateConfig {

  val fname = config.getString("dir.spark.data")+"/affiliations/affiliations.json"
  MongoCollectionToSingleLineJson.streamAll(MongoDbAffiliations.collection, fname)
    .onComplete({ _ => System.exit(0) })

}
