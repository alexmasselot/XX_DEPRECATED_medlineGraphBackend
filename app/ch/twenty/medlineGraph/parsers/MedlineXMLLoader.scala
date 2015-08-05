package ch.twenty.medlineGraph.parsers

import java.io.{FileInputStream, BufferedInputStream}
import java.util.zip.GZIPInputStream

import scala.xml.{XML, Node}

/**
 * Load an xml file and get out an iterator of MedlineCitation XML nodes.
 * works for pubmed or medline xml structures
 *
 * @author Alexandre Masselot.
 */


class MedlineXMLLoader(filename: String) {


  /**
   * get a dtd parser without relying on the DTD file. If no network is available, the default one cannot proceed
   * @return
   */
  def dtdLessParser = {
    val f = javax.xml.parsers.SAXParserFactory.newInstance()
    f.setValidating(false)
    f.newSAXParser()
  }

  def getInputSteam = {
    if (filename.endsWith(".gz")) {
      new GZIPInputStream(new BufferedInputStream(new FileInputStream(filename)))
    } else {
      new FileInputStream(filename)
    }
  }

  def getDoc =
    xml.XML.withSAXParser(dtdLessParser).load(getInputSteam)
//
//
//  try {
//    XML.load(getInputSteam)
//  } catch {
//    case _:Throwable => try {
//      xml.XML.withSAXParser(dtdLessParser).load(getInputSteam)
//    } catch {
//      case e: java.net.UnknownHostException => xml.XML.withSAXParser(dtdLessParser).load(getInputSteam)
//      case e:Throwable => throw e
//    }
//  }

  /**
   * get the list of MedlineCitation xml nodes
   * @return
   */
  def iteratorCitation: Iterator[Node] = {
    val doc = getDoc

    //val doc = xml.XML.withSAXParser(dtdLessParser).load(filename)

    val it = (doc \ "PubmedArticle" \ "MedlineCitation") ++ (doc \ "MedlineCitation")
    it.iterator
  }
}


