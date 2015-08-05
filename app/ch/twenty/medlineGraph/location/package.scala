package ch.twenty.medlineGraph

import ch.twenty.medlineGraph.models.{Country, City}

/**
 * @author Alexandre Masselot.
 */
package object location {
  case class GeoNameId(value:Int) extends AnyVal

  case class CountryInfoIso(value:String) extends AnyVal
  case class CountryInfoIso3(value:String) extends AnyVal

  case class GeoCoordinates(latitude:Double, longitude:Double)
  case class Location(city: City, country: Country, coordinates: GeoCoordinates)
}