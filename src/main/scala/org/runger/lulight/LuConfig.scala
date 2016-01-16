package org.runger.lulight

import java.net.URL
import play.api.libs.json.{Json, JsValue, Writes}

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

  def areas = loads.map(_.areaName)

  val byId = loads.map(l => l.id -> l).toMap
}

object LightingLoad {
//  implicit val lightingLoadWrites = new Writes[LightingLoad] {
//    override def writes(o: LightingLoad): JsValue =
//  }
  implicit val lightingLoadFormat = Json.format[LightingLoad]
}

case class LightingLoad(id: Int, areaName: String, outputName: String) {
  def off() = {
    s"#OUTPUT,$id,1,0"
  }
  def on() = {
    s"#OUTPUT,$id,1,100"
  }
  def set(level: Int) = {
    s"#OUTPUT,$id,1,$level"
  }
  def getState() = {
    s"?OUTPUT,$id,1"
  }
}

case class LoadMeta(luId: Int, name: String, floor: Int, public: Boolean, led: Boolean, interior: Boolean)

object MetaConfig extends Logging {
  val public = true
  val priv = false
  val led = true
  val inc = false
  val interior = true
  val exterior = false

  val meta = Set(
    LoadMeta(0, "Nothing", -1, priv, led, interior)
    ,LoadMeta(65, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(56, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(55, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(72, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(54, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(48, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(50, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(49, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(95, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(93, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(94, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(96, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)

  )


}

object LuConfig extends Logging {

  val repeaterIpAddress = "192.168.1.2"

  val luConfigXml = XML.load(new URL(s"http://$repeaterIpAddress/DbXmlInfo.xml"))

  def parseXml() = {
    val ll = for {
      area <- luConfigXml \\ "Area" if !area.attribute("Name").exists(_.exists(_.text == "MonteMar"))
      output <- area \\ "Output"
      areaName <- area.attribute("Name").toSeq.flatten
      outputName <- output.attribute("Name").toSeq.flatten
      id <- output.attribute("IntegrationID").toSeq.flatten
    } yield LightingLoad(id.text.toInt, areaName.text, outputName.text)
    ll.foreach(l => info(l.toString))
    LoadSet(ll.toSet)
  }

  def apply() = parseXmlOnce

  var parseXmlOnce = parseXml()

  def reload() = {
    parseXmlOnce = parseXml()
    parseXmlOnce
  }

}

object LuConfigTest extends App {
  LuConfig.parseXml
}
