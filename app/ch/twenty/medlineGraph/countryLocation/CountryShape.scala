package ch.twenty.medlineGraph.countryLocation

import java.awt.Polygon

import ch.twenty.medlineGraph.location.CountryInfoIso

/**
 * @author Alexandre Masselot.
 */
case class CountryShape(val countryCode:CountryInfoIso, polygons:List[Polygon]) {

}
