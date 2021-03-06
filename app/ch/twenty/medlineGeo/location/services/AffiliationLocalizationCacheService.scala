package ch.twenty.medlineGeo.location.services

import ch.twenty.medlineGeo.location.Location
import ch.twenty.medlineGeo.models.AffiliationInfo
import ch.twenty.medlineGeo.parsers.AffiliationInfoParser

import scala.util.{Failure, Success, Try}
import play.api.Play.current
import play.api.cache._
import play.api.mvc._
import javax.inject.Inject

case class CacheMisMatchException(key: String) extends Exception(key)

/**
 * caches affiliationInfo
 */
class AffiliationLocalizationCacheService extends AffiliationLocalizationService {
  val isBulkOnly=false
  //TODO put a real cache
  val cache = scala.collection.mutable.Map[String, Location]()

  def getKey(affiliationInfo: AffiliationInfo) = AffiliationInfoParser.firstSentence(affiliationInfo)

  /**
   *
   * @param affiliationInfo
   * @return
   */
  def locate(affiliationInfo: AffiliationInfo): Try[Location] = {
    val key = getKey(affiliationInfo)
    cache.get(key) match {
      case Some(loc) => Success(loc)
      case None => Failure(CacheMisMatchException(key))
    }
  }

  /**
   * add a location in the cache
   * @param affiliationInfo
   * @param location
   * @return
   */
  def put(affiliationInfo: AffiliationInfo, location: Location) = {
    val key = getKey(affiliationInfo)
    cache.put(key, location)
  }
}
