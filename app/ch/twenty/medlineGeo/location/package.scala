package ch.twenty.medlineGeo

import ch.twenty.medlineGeo.models.{Country, City}

/**
 * @author Alexandre Masselot.
 */
package object location {
  case class GeoNameId(value:Int) extends AnyVal

  case class CountryInfoIso(value:String) extends AnyVal
  case class CountryInfoIso3(value:String) extends AnyVal

  case class GeoCoordinates(latitude:Double, longitude:Double){
    override def toString = s"($latitude, $longitude)"
  }
  case class Location(city: Option[City], countryIso: Option[CountryInfoIso], coordinates: GeoCoordinates)
}
