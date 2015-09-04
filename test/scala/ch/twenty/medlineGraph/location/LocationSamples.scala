package ch.twenty.medlineGraph.location

import ch.twenty.medlineGraph.location.countryLocation.CountryShapeList

/**
 * @author Alexandre Masselot.
 */
trait LocationSamples {
  val filenameCities = "test/resources/cities-samples.txt"
  val filenameCountries = "test/resources/countryInfo-samples.txt"
  val filenameAlternateNames = "test/resources/alternateNames-samples.txt"
  val filenameCountryShapes = "test/resources/geonames-country-shapes.txt"

  def loadDir = CityDirectory.load(filenameCities, loadCountryDir)
  def loadAlternateNameDir = AlternateNameDirectory.load(filenameAlternateNames)
  def loadCountryDir = CountryDirectory.load(filenameCountries, loadAlternateNameDir)
  def loadCountryShapes = CountryShapeList.load(filenameCountryShapes, loadCountryDir)

}
