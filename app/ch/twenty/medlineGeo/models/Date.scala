package ch.twenty.medlineGeo.models

/**
 * Date with optional month and Day
 * @author Alexandre Masselot.
 */
case class Date(year: Option[Int], month: Option[Int], day: Option[Int]) {
  //override def toString = s"""${year.getOrElse("-")}/${month.getOrElse("-")}/${day.getOrElse("-")}"""
}

object DateParser {
  val reNumber_2 = """(\d{1,2})""".r
  val reNumber_4 = """(\d\d\d\d)""".r

  val months = Map("jan" -> 1,
    "feb" -> 2,
    "mar" -> 3,
    "apr" -> 4,
    "may" -> 5,
    "jun" -> 6,
    "jul" -> 7,
    "aug" -> 8,
    "sep" -> 9,
    "oct" -> 10,
    "nov" -> 11,
    "dec" -> 12
  )

  def parse(year: String, month: String, day: String): Date = {
    Date(
      year match {
        case reNumber_4(i) => Some(i.toInt)
        case _ => None
      },
      month match {
        case reNumber_2(i) => Some(i.toInt)
        case x => months.get(month.toLowerCase)
      },
      day match {
        case reNumber_2(i) => Some(i.toInt)
        case _ => None
      }
    )
  }
}
