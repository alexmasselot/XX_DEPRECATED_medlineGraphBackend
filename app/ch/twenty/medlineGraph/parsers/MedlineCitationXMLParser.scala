package ch.twenty.medlineGraph.parsers

import ch.twenty.medlineGraph.models._

import scala.util.{Failure, Success, Try}
import scala.xml.Node

/**
 * Parses MedlineCitation nodes information into Citation (or failures)
 *
 * @author Alexandre Masselot.
 */
object MedlineCitationXMLParser {
  val reMultiLine = """\s*\n\s*""".r
  def lineSinglify(str:String) = {
    reMultiLine.replaceAllIn(str," ").trim
  }
  
  /**
   * one XML node into a potential Citation
   * @param node
   * @return
   */
  def apply(node: Node): Try[Citation] = {
    try {
      val pmid = node \ "PMID" text
      val nArticle = node \ "Article"
      val abstractText = lineSinglify(nArticle \ "Abstract" \ "AbstractText" text)
      val title = lineSinglify(nArticle \ "ArticleTitle" text)

      val nPubDate = nArticle \ "Journal" \ "JournalIssue" \ "PubDate"
      val pubDate = DateParser.parse(nPubDate \ "Year" text,
        nPubDate \ "Month" text,
        nPubDate \ "Day" text
      )
      val authors = for {
        e <- nArticle \ "AuthorList" \ "Author"
      } yield {
          val affiliation = (e \ "AffiliationInfo" \ "Affiliation").map(x => AffiliationInfoParser(lineSinglify(x.text))).headOption
          Author(
            LastName(e \ "LastName" text),
            ForeName(e \ "ForeName" text),
            Initials(e \ "Initials" text),
            affiliation
          )
        }
      Success(Citation(PubmedId(pmid), pubDate, Title(title), AbstractText(abstractText), authors))
    } catch {
      case e: Throwable => Failure(e)
    }
  }

  /**
   * an iterator of XML nodes into one of Citation
   * @param it
   * @return
   */
  def apply(it: Iterator[Node]): Iterator[Try[Citation]] = it.map(apply)

  /**
   * transform an iterator of XML nodes into two iterators:
   * * one with all the parsed citations
   * * one with all the parsing failures
   * @param it
   * @return
   */
  def iteratorsCitationFailures(it: Iterator[Node]): (Iterator[Citation], Iterator[Throwable]) = {
    val (itSucc, itFail) = apply(it).partition(_.isSuccess)
    (itSucc.map(_.get), itFail.map(_.failed.get))
  }
}
