package ch.twenty.medlineGraph.location

import ch.twenty.medlineGraph.models._
import org.apache.commons.lang3.StringUtils

import scala.io.Source
import scala.util.{Success, Failure, Try}


/**
 * @author Alexandre Masselot.
 */

case class NoCityLocationException(city: City, country: Country) extends Exception(s"[${city.value}], [${country.value}]")

case class MultipleCityLocationException(city: City, country: Country, n: Int) extends Exception(s"[${city.value}], [${country.value}] ($n matches)")

case class CityLocation(city: City, country: Country, coordinates: GeoCoordinates) {

}

case class CityRecord(id: GeoNameId, city: City, countryCode: CountryInfoIso, coordinates: GeoCoordinates)

class CityDirectory(records: Seq[CityRecord], countryDirectory: CountryDirectory, alternateNameDirectory: AlternateNameDirectory) {
  val naCountry = Country("-")

  def size = records.size

  lazy val cityDict = records.toList
    .groupBy(rec => CityDirectory.projectCity(rec.city))

  lazy val idDictionary: Map[GeoNameId, CityRecord] = records.map(x => (x.id, x)).toMap

  def get(city: City):Option[List[CityRecord]] = cityDict.get(CityDirectory.projectCity(city))

  def exists(city: City) = cityDict.get(CityDirectory.projectCity(city)).isDefined

  def getUniqueFromCityAndUnknownCountry(city: City, country: Country): Try[CityLocation] = {
    (cityDict.get(CityDirectory.projectCity(city)), countryDirectory.countryExists(country)) match {
      case (Some(rec :: Nil), false) =>
        Success(CityLocation(rec.city, naCountry, rec.coordinates))
      case _ => Failure(new Exception())
    }
  }

  def getDirectFromCityCountryDirect(city: City, country: Country): Try[CityLocation] = cityDict.get(CityDirectory.projectCity(city)) match {
    case Some(records) => records.filter(r => countryDirectory.get(r.countryCode).map(_.country == country).getOrElse(false)) match {
      case (rec :: Nil) => Success(CityLocation(rec.city, country, rec.coordinates))
      case _ => Failure(new Exception())
    }
    case _ => Failure(new Exception())
  }

  def getDirectFromCityIsCountry(city: City, country: Country): Try[CityLocation] = {
    val cityCountry = City(country.value)
    if (exists(cityCountry)) {
      getDirectFromCityCountryDirect(cityCountry, country)
    } else {
      Failure(new Exception())
    }
  }

  def getDirectFromCityCountrySynonymous(city: City, country: Country): Try[CityLocation] = {
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
      .toList

    potentialCityRecords match {
      case Nil => Failure(NoCityLocationException(city, country))
      case (x :: Nil) => Success(CityLocation(x._1.city, country, x._1.coordinates))
      case (x :: xs) => Failure(MultipleCityLocationException(x._1.city, country, xs.size + 1))
    }
  }

  def getFromCityCountry(city: City, country: Country): Try[CityLocation] = {
    val lFunct = List(getUniqueFromCityAndUnknownCountry _, getDirectFromCityCountryDirect _, getDirectFromCityIsCountry _, getDirectFromCityCountrySynonymous _)

    def getFromCityCountryHandler(fStill: List[(City, Country) => Try[CityLocation]]): Try[CityLocation] = fStill match {
      case f :: Nil => f(city, country)
      case f :: fs => f(city, country) match {
        case Success(r) => Success(r)
        case _ => getFromCityCountryHandler(fs)
      }
      case Nil => Failure(new IllegalArgumentException("cannot provide no function, Dude"))
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
  def apply(city: City, country: Country): Try[CityLocation] = {
    getFromCityCountry(city, country) match {
      case Success(loc) => Success(loc)
      case Failure(e) => {
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
      CityRecord(GeoNameId(tmp(0).toInt), City(tmp(1)), CountryInfoIso(tmp(8)), GeoCoordinates(tmp(4).toDouble, tmp(5).toDouble))
    })

    val mSyno= Source.fromFile(cityFilename)
    .getLines()
    .map({ line =>
      val tmp = line.split("\t").toVector
      val id = GeoNameId(tmp(0).toInt)
      (id, tmp(3).split(",").toList)
    }).toMap

    new CityDirectory(itRecords.toSeq, countryDirectory, new AlternateNameDirectory(mSyno))
  }
}