package ch.twenty.medlineGraph.models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
 * serialize object to/from Json
 *
 * @author Alexandre Masselot.
 */
object JsonSerializer {

  implicit val formatPubmedId = new Format[PubmedId] {
    override def writes(o: PubmedId): JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[PubmedId] = JsSuccess(PubmedId(json.as[String]))
  }

  implicit val formatLastName = new Format[LastName] {
    override def writes(o: LastName): JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[LastName] = JsSuccess(LastName(json.as[String]))
  }
  implicit val formatForeName = new Format[ForeName] {
    override def writes(o: ForeName): JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[ForeName] = JsSuccess(ForeName(json.as[String]))
  }
  implicit val formatInitials = new Format[Initials] {
    override def writes(o: Initials): JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[Initials] = JsSuccess(Initials(json.as[String]))
  }
  implicit val formatTitle = new Format[Title] {
    override def writes(o: Title): JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[Title] = JsSuccess(Title(json.as[String]))
  }
  implicit val formatInstitution = new Format[Institution] {
    override def writes(o: Institution): JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[Institution] = JsSuccess(Institution(json.as[String]))
  }
  implicit val formatCity = new Format[City] {
    override def writes(o: City): JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[City] = JsSuccess(City(json.as[String]))
  }
  implicit val formatCountry = new Format[Country] {
    override def writes(o: Country): JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[Country] = JsSuccess(Country(json.as[String]))
  }
  implicit val formatAbstractText = new Format[AbstractText] {
    override def writes(o: AbstractText): JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[AbstractText] = JsSuccess(AbstractText(json.as[String]))
  }
  //  implicit val formatXXX = new Format[XXX] {
  //    override def writes(o: XXX): JsValue = JsString(o.value)
  //    override def reads(json: JsValue): JsResult[XXX] = JsSuccess(XXX(json.as[String]))
  //  }

  implicit val formatDate: Format[Date] = {
    val dateReads: Reads[Date] =
      (
        (JsPath \ "year").readNullable[Int] and
          (JsPath \ "month").readNullable[Int] and
          (JsPath \ "day").readNullable[Int]
        )(Date.apply _)

    val dateWrites: Writes[Date] = (
      (JsPath \ "year").writeNullable[Int] and
        (JsPath \ "month").writeNullable[Int] and
        (JsPath \ "rodayle").writeNullable[Int]
      )(unlift(Date.unapply))

    Format(dateReads, dateWrites)
  }

  implicit val formatAffiliationInfo = Json.format[AffiliationInfo]
  implicit val formatAuthor = Json.format[Author]


  implicit val formatCitation = Json.format[Citation]


}
