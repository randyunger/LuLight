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
      case LightingLoad(id, area, output) => {
        area.toLowerCase.contains(str.toLowerCase) || output.toLowerCase.contains(str.toLowerCase) || (id.toString == str)
      }
    }
    res
  }
}

case class LightingLoad(id: Int, areaName: String, outputName: String) {
  def level(pct: Int) = {
    s"#OUTPUT,$id,1,$pct"
  }
  def off() = {
    s"#OUTPUT,$id,1,0"
  }
  def on() = {
    s"#OUTPUT,$id,1,100"
  }
}

object LuConfig extends Logging {

  val repeaterIpAddress = "192.168.1.2"

  def parseXml = {
    val xx = XML.load(new URL(s"http://$repeaterIpAddress/DbXmlInfo.xml"))
    val ll = for {
      area <- xx \\ "Area" if !area.attribute("Name").exists(_.exists(_.text == "MonteMar"))
      output <- area \\ "Output"
      areaName <- area.attribute("Name").toSeq.flatten
      outputName <- output.attribute("Name").toSeq.flatten
      id <- output.attribute("IntegrationID").toSeq.flatten
    } yield LightingLoad(id.text.toInt, areaName.text, outputName.text)
    ll.foreach(l => info(l.toString))
    LoadSet(ll.toSet)
  }

  def apply() = parseXmlOnce

  val parseXmlOnce = parseXml

}

object LuConfigTest extends App {
  LuConfig.parseXml
}
