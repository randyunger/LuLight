package org.lulight

import java.net.URL
import scala.xml.XML

/**
 * Created by Unger on 9/30/15.
 */

case class LoadSet(loads: Set[LightingLoad]) {
  def cans = {
    loads.filter(_.outputName.toLowerCase.contains("cans"))
  }

  def search(str: String) = {
    val res = loads.filter {
      case LightingLoad(id, a, o) => {
        a.toLowerCase.contains(str.toLowerCase) || o.toLowerCase.contains(str.toLowerCase)
      }
    }
    res
  }
}

case class LightingLoad(id: Int, areaName: String, outputName: String) {
  def level(pct: Int) = {
    s"#OUTPUT,$id,1,$pct"
  }
}

object LuConfig {
  def parseXml = {
    val xx = XML.load(new URL("http://192.168.1.147/DbXmlInfo.xml"))
    val ll = for {
      area <- xx \\ "Area" if !area.attribute("Name").exists(_.exists(_.text == "MonteMar"))
      output <- area \\ "Output"
      areaName <- area.attribute("Name").toSeq.flatten
      outputName <- output.attribute("Name").toSeq.flatten
      id <- output.attribute("IntegrationID").toSeq.flatten
    } yield LightingLoad(id.text.toInt, areaName.text, outputName.text)
    ll.foreach(println)
    LoadSet(ll.toSet)
  }
}

object LuConfigTest extends App {
  LuConfig.parseXml
}
