package ch.twenty.medlineGraph.location

import ch.twenty.medlineGraph.models.Country

import scala.io.Source

/**
 * We have to reconciliate endless synonyms (english version only)
 *
 * @author Alexandre Masselot.
 */
class AlternateNameDirectory(dict: Map[GeoNameId, List[String]]) {
  /**
   *
   * @return
   */
  def size = dict.size

  /**
   *
   */
  lazy val name2id: Map[String, List[GeoNameId]] =
    dict.toList
      .flatMap({ case (id, names) =>
      names.map(n => (n, id))
    })
      .groupBy(_._1)
      .map({ case (name, xs) => (name, xs.map(_._2)) })

  /**
   * from an id, get the list of name
   * @param id
   * @return
   */
  def apply(id: GeoNameId): List[String] = dict(id)

  /**
   * from an id, get the list of countries
   * @param id
   * @return
   */
  def get(id: GeoNameId): Option[List[String]] = dict.get(id)

  /**
   * for a given name, returns the synonymous ids
   * @param name
   * @return
   */
  def getSynonymousIds(name:String):Option[List[GeoNameId]] = name2id.get(name)
}

case class CannotParseAlternateNameLine(line: String) extends Exception(line)

object AlternateNameDirectory {

  case class AlternateNameRecord(id: Int, targetId: Int, lang: String, name: String)

  def load(filename: String): AlternateNameDirectory = {
    val map = Source.fromFile(filename)
      .getLines()
      .map(_.split("\t").toList match {
      case id :: jd :: lang :: str :: xs => AlternateNameRecord(id.toInt, jd.toInt, lang, str)
      case x => throw CannotParseAlternateNameLine(x.mkString("\t"))
    })
      .filter(x => x.lang == "en" || x.lang == "abbr")
      .toList
      .groupBy(_.targetId)
      .map({ case (id, records) => (GeoNameId(id), records.map(x => x.name)) })

    new AlternateNameDirectory(map)
  }
}

