package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location.Location
import ch.twenty.medlineGraph.models.AffiliationInfo

import scala.util.Try

/**
 * common trait among affiliationInfo
 * @author Alexandre Masselot.
 */
trait AffiliationLocalizationService {
  /**
   * from one affiliationInfo, tries to locate it.
   * This is the method to implement
   * @param affiliationInfo
   * @return
   */
  def locate(affiliationInfo: AffiliationInfo): Try[Location]

  def locate(affiliationInfos: Traversable[AffiliationInfo]): Traversable[Try[Location]] = affiliationInfos.map(locate)

}
