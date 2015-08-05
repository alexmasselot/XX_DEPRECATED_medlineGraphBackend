package ch.twenty.medlineGraph.location

/**
 * @author Alexandre Masselot.
 */
trait LocationSamples {

  def loadDir = CityDirectory.load("test/resources/cities-samples.txt", loadCountryDir)
  def loadAlternateNameDir = AlternateNameDirectory.load("test/resources/alternateNames-samples.txt")
  def loadCountryDir = CountryDirectory.load("test/resources/countryInfo-samples.txt", loadAlternateNameDir)

}
