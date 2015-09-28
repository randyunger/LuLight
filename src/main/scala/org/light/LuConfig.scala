package org.light

import java.net.URL

import scala.xml.XML

/**
 * Created by runger on 9/27/15.
 */

case class LightingLoad(id: Int, areaName: String, outputName: String)

object LuConfig {
  def parseXml = {
    val xx = XML.load(new URL("http://192.168.1.147/DbXmlInfo.xml"))
    val ll = for {
      area <- (xx \\ "Area") if !area.attribute("Name").exists(_.exists(_.text == "MonteMar"))
      output <- area \\ "Output"
      areaName <- area.attribute("Name").toSeq.flatten
      outputName <- output.attribute("Name").toSeq.flatten
      id <- output.attribute("IntegrationID").toSeq.flatten
    } yield LightingLoad(id.text.toInt, areaName.text, outputName.text)
    ll.foreach(println)
    ll.toSet
  }
}

object LuConfigTest extends App {
  LuConfig.parseXml
}