package ch.twenty.medlineGraph.location

import ch.twenty.medlineGraph.models.Country

import scala.io.Source

/**
 * @author Alexandre Masselot.
 */

case class CountryInfoRecord(id: GeoNameId, iso: CountryInfoIso, ios3: CountryInfoIso3, country: Country)

case class CountryDirectory(records: Seq[CountryInfoRecord], alternateNames: AlternateNameDirectory) {
  /**
   * get the number of registered countries
   * @return
   */
  def size = records.size


  lazy val isoDictionary: Map[CountryInfoIso, CountryInfoRecord] = records.map(x => (x.iso, x)).toMap
  lazy val country2record = records.map(_.country).toSet
  lazy val idDictionary: Map[GeoNameId, CountryInfoRecord] = records.map(x => (x.id, x)).toMap

  def countryExists(country:Country):Boolean =  country2record.contains(country)

  /**
   * get by Iso code
   */
  def apply(code: CountryInfoIso): CountryInfoRecord = isoDictionary(code)

  def get(code: CountryInfoIso): Option[CountryInfoRecord] = isoDictionary.get(code)

  def findById(id: GeoNameId): Option[CountryInfoRecord] = idDictionary.get(id)

  /**
   * check if the passed ISO code is synomous to the given country
   * @param code
   * @param country
   * @return
   */
  def isSynonymous(code: CountryInfoIso, country: Country): Boolean = {
    (apply(code).country == country) ||
      (alternateNames.get(apply(code).id) match {
        case Some(l) => l.map(x => Country(x)).contains(country)
        case None => false
      })
  }
}

object CountryDirectory {
  def load(filename: String = "resources/countryInfo.txt", alternateNameDirectory: AlternateNameDirectory): CountryDirectory = {
    val map = Source.fromFile(filename)
      .getLines()
      .filterNot(_.startsWith("#"))
      .map({ line =>
      val tmp = line.split("\t").toVector
      val id = if(tmp(16)=="")0 else tmp(16).toInt
      CountryInfoRecord(GeoNameId(id), CountryInfoIso(tmp(0)), CountryInfoIso3(tmp(1)), Country(tmp(4)))
    })
    .toSeq

    CountryDirectory(map, alternateNameDirectory)
  }
}