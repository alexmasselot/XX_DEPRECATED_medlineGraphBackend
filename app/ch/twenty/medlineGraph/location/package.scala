package ch.twenty.medlineGraph

/**
 * @author Alexandre Masselot.
 */
package object location {
  case class GeoNameId(value:Int) extends AnyVal

  case class CountryInfoIso(value:String) extends AnyVal
  case class CountryInfoIso3(value:String) extends AnyVal

  case class GeoCoordinates(latitude:Double, longitude:Double)
}
