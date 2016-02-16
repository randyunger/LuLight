package org.runger.lulight

import java.net.URL
import play.api.libs.json.{Json, JsValue, Writes}

import scala.xml.{Elem, XML}

/**
 * Created by Unger on 9/30/15.
 */

case class LoadSet(loads: Set[LightingLoad]) {
  def cans = {
    loads.filter(_.outputName.toLowerCase.contains("cans"))
  }

  def search(str: String) = {
    val res = loads.filter {
      case LightingLoad(id, area, output, _) => {
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

case class LightingLoad(id: Int, areaName: String, outputName: String, meta: Option[LoadMeta]) {
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

object LuConfig extends Logging {
  val repeaterIpAddress = "192.168.1.2"
  lazy val defaultFetcher = XML.load(new URL(s"http://$repeaterIpAddress/DbXmlInfo.xml"))
  lazy val prodInstance = new LuConfig(defaultFetcher)
  val locConfig =
    <Area Name ="Kitchen">
      <Output Name ="Kitchen Cans" IntegrationID="1"></Output>
    </Area>
  val locInstance = new LuConfig(locConfig)

  def apply() = if(Settings().localOnly) {
    info("using local instance")
    locInstance
  } else {
    info("using prod instance")
    prodInstance
  }
}

class LuConfig(configFetcher: => Elem) extends Logging {

  val luConfigXml = configFetcher

  def parseXml() = {
    val ll = for {
      area <- luConfigXml \\ "Area" if !area.attribute("Name").exists(_.exists(_.text == "MonteMar"))
      output <- area \\ "Output"
      areaName <- area.attribute("Name").toSeq.flatten
      outputName <- output.attribute("Name").toSeq.flatten
      id <- output.attribute("IntegrationID").toSeq.flatten
    } yield {
      val idInt = id.text.toInt
      val meta = MetaConfig.byId(idInt)
      LightingLoad(idInt, areaName.text, outputName.text, meta)
    }
    ll.foreach(l => info(l.toString))
    LoadSet(ll.toSet)
  }

  def state() = parseXmlOnce

  var parseXmlOnce = parseXml()

  def reload() = {
    parseXmlOnce = parseXml()
    parseXmlOnce
  }

}

object LuConfigTest extends App {
  LuConfig().parseXml
}
