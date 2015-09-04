package ch.twenty.medlineGraph.location

import ch.twenty.medlineGraph.models._
import org.apache.commons.lang3.StringUtils

import scala.io.Source
import scala.util.{Success, Failure, Try}


/**
 * @author Alexandre Masselot.
 */

case class NoCityLocationException(city: City, country: Country) extends Exception(s"[${city.value}], [${country.value}]")


case class CityCountryIncompatibilityException(city: City, country: Country) extends Exception(s"[${city.value}], [${country.value}]")

case class MultipleCityLocationException(city: City, country: Country, n: Int) extends Exception(s"[${city.value}], [${country.value}] ($n matches)")


case class CityRecord(id: GeoNameId, city: City, countryCode: CountryInfoIso, coordinates: GeoCoordinates, population:Int)

class CityDirectory(records: Seq[CityRecord], countryDirectory: CountryDirectory, alternateNameDirectory: AlternateNameDirectory) {
  val populationResolutionFactor=10.0

  def size = records.size

  lazy val cityDict = records.toList
    .groupBy(rec => CityDirectory.projectCity(rec.city))

  lazy val idDictionary: Map[GeoNameId, CityRecord] = records.map(x => (x.id, x)).toMap

  def get(city: City): Option[List[CityRecord]] = cityDict.get(CityDirectory.projectCity(city))

  def exists(city: City) = cityDict.get(CityDirectory.projectCity(city)).isDefined


  /**
   * from a list of cities, keep all of them where the population is >= max/populationResolutionFactor. The idea is to keep only one if there one way more popular than the other
   * @param records citi records
   * @return a subset of the input list. never empty
   */
  def keepMostPopulatedCities(records:List[CityRecord]): List[CityRecord] = records match {
    case Nil => Nil
    case _ =>
      val sortedRecords = records.sortBy(-_.population)
      val thres = sortedRecords.head.population/populationResolutionFactor
      sortedRecords.filter (_.population >= thres)
  }

  def getUniqueFromCityAndUnknownCountry(city: City, country: Country): Try[Location] = {
    (cityDict.get(CityDirectory.projectCity(city)), countryDirectory.countryExists(country)) match {
      case (Some(rec :: Nil), false) =>
        Success(Location(Some(rec.city), Some(rec.countryCode), rec.coordinates))
      case _ => Failure(NoCityLocationException(city, country))
    }
  }

  /**
   * city is labelled as country but yet is uniquevocal
   * @param city target city
   * @param country target country
   * @return
   */
  def getUniqueFromCityLabeledAsCountry(city: City, country: Country): Try[Location] = {
    val cityFromCountryVal = City(country.value)

    (cityDict.get(CityDirectory.projectCity(cityFromCountryVal)).map(keepMostPopulatedCities), countryDirectory.countryExists(country)) match {
      case (Some(rec :: Nil), false) =>
        Success(Location(Some(rec.city), Some(rec.countryCode), rec.coordinates))
      case _ => Failure(NoCityLocationException(cityFromCountryVal, Country("")))
    }
  }

  def getDirectFromCityCountryDirect(city: City, country: Country): Try[Location] = cityDict.get(CityDirectory.projectCity(city)) match {
    case Some(records) => records.filter(r => countryDirectory.get(r.countryCode).exists(_.country == country)) match {
      case (rec :: Nil) => Success(Location(Some(rec.city), Some(rec.countryCode), rec.coordinates))
      case _ => Failure(NoCityLocationException(city, country))
    }
    case _ => Failure(NoCityLocationException(city, country))
  }

  /**
   * does not resolve anything, but send CityCountryIncompatibilityException if both citry and counbtry exists but are incompatible
   * @param city target city
   * @param country target country
   * @return
   */
  def getDirectFromCityCountryAreIncomptible(city: City, country: Country): Try[Location] = {
    val cityRecords = cityDict.get(CityDirectory.projectCity(city))
    if (countryDirectory.countryExists(country)) {
      cityRecords match {
        case Some(records) =>
          if (records.nonEmpty && records.forall(r => countryDirectory.get(r.countryCode).exists(_.country != country))) {
            Failure(CityCountryIncompatibilityException(city, country))
          } else {
            Failure(NoCityLocationException(city, country))
          }
        case _ => Failure(NoCityLocationException(city, country))
      }
    } else {
      Failure(NoCityLocationException(city, country))
    }
  }


  def getDirectFromCityIsCountry(city: City, country: Country): Try[Location] = {
    val cityCountry = City(country.value)
    if (exists(cityCountry)) {
      getDirectFromCityCountryDirect(cityCountry, country)
    } else {
      Failure(new NoCityLocationException(cityCountry, country))
    }
  }

  def getDirectFromCityCountrySynonymous(city: City, country: Country): Try[Location] = {
    val matchCity = CityDirectory.projectCity(city)

    val potentialRecords = cityDict.get(matchCity) match {
      case Some(l) => l
      case None => Nil
    }

    val potentialCityRecords = potentialRecords
      .map({ r =>
      (r, countryDirectory(r.countryCode))
    })
      .filter(x => countryDirectory.isSynonymous(x._1.countryCode, country))


    keepMostPopulatedCities(potentialCityRecords.map(_._1)) match {
      case Nil => Failure(NoCityLocationException(city, country))
      case (x :: Nil) => Success(Location(Some(x.city), Some(x.countryCode), x.coordinates))
      case (x :: xs) => Failure(MultipleCityLocationException(x.city, country, xs.size + 1))
    }
  }

  def getFromCityCountry(city: City, country: Country): Try[Location] = {

    val lFunct = List(
      getDirectFromCityCountryDirect _,
      getDirectFromCityCountryAreIncomptible _,
      getUniqueFromCityAndUnknownCountry _,
      getDirectFromCityIsCountry _,
      getDirectFromCityCountrySynonymous _,
      getUniqueFromCityLabeledAsCountry _
    )

    def getFromCityCountryHandler(fStill: List[(City, Country) => Try[Location]]): Try[Location] = {
      fStill match {
        case f :: Nil => f(city, country)
        case f :: fs => f(city, country) match {
          case Success(r) => Success(r)
          case Failure(e: CityCountryIncompatibilityException) =>
            Failure(e)
          case _ => getFromCityCountryHandler(fs)
        }
        case Nil => Failure(new IllegalArgumentException("cannot provide no function, Dude"))
      }
    }
    getFromCityCountryHandler(lFunct)
  }


  /**
   * from a city/country, give a success CityLocation (so, add the latitude/longitude) only if a unique match was  found in the target DB
   * if the city is know in the directory, take it. Else, it will start looking into the synonymous
   * @param city the target city (case and accent are not important)
   * @param country the target country
   * @return Failure will carry info about no or multi matches
   */
  def apply(city: City, country: Country): Try[Location] = {
    getFromCityCountry(city, country) match {
      case Success(loc) => Success(loc)
      case Failure(e) =>
        alternateNameDirectory.getSynonymousIds(city.value) match {
          case None => Failure(e)
          case Some(l) => l.map(idDictionary.get)
            .filter(_.isDefined)
            .map(_.get)
            .toIterator
            .map(rec => getFromCityCountry(rec.city, country))
            .find(_.isSuccess)
            .getOrElse(Failure(e))
        }
    }
  }
}

object CityDirectory {
  /**
   * make a simpler projection (removes accent, go to upper case etc.)
   * @param city the input city
   * @return
   */
  def projectCity(city: City) = City(StringUtils.stripAccents(city.value).toUpperCase)

  def load(cityFilename: String = "resources/cities15000.txt", countryDirectory: CountryDirectory): CityDirectory = {
    val itRecords = Source.fromFile(cityFilename)
      .getLines()
      .map({ line =>
      val tmp = line.split("\t").toVector
      CityRecord(GeoNameId(tmp.head.toInt), City(tmp(1)), CountryInfoIso(tmp(8)), GeoCoordinates(tmp(4).toDouble, tmp(5).toDouble), tmp(14).toInt)
    })

    val mSyno = Source.fromFile(cityFilename)
      .getLines()
      .map({ line =>
      val tmp = line.split("\t").toVector
      val id = GeoNameId(tmp.head.toInt)
      (id, tmp(3).split(",").toList)
    }).toMap

    new CityDirectory(itRecords.toSeq, countryDirectory, new AlternateNameDirectory(mSyno))
  }
}