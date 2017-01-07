package org.runger.lulight

import java.io.FileInputStream
import java.net.URL
import org.joda.time.DateTime
import play.api.libs.json.{Json, JsValue, Writes}

import scala.xml.{Elem, XML}

/**
 * Created by Unger on 9/30/15.
 */

object LoadSet {
  val empty = LoadSet(Set.empty)
}

case class LoadSet(loads: Set[LightingLoad]) {
//  def cans = {
//    loads.filter(_.outputName.toLowerCase.contains("cans"))
//  }

  def search(str: String) = {
    val res = loads.filter {
      case LightingLoad(id, area, output, _, _) => {
        area.toLowerCase.contains(str.toLowerCase) || output.toLowerCase.contains(str.toLowerCase) || (id.toString == str)
      }
    }
    res
  }

  def areas = loads.map(_.areaName)

  val byId = loads.map(l => l.id -> l).toMap

  val nonZeros = loads.filter(ll => ll.state.exists(_.level!=0))

//  def filterBy(fs: FilterSet) = {
//    val filteredLoads = loads.filter(ll => {
//      fs.bulbTypes.map(allowed => ll.meta.exists(load => load.bulb == allowed)).getOrElse(true) &&
//      fs.floors.map(req => ll.meta.exists(load => load.floor == req)).getOrElse(true) &&
//      fs.intExts.map(req => ll.meta.exists(load => load.intExt == req)).getOrElse(true)
//    })
//    LoadSet(filteredLoads)
//  }

  //Get state from cache if available
  def withState = LoadSet(loads.map(LuStateTracker().withState))
}

object LightingLoad {
//  implicit val lightingLoadWrites = new Writes[LightingLoad] {
//    override def writes(o: LightingLoad): JsValue =
//  }
  implicit val lightingLoadFormat = Json.format[LightingLoad]
}

case class LightingLoad(id: Int, areaName: String, outputName: String, meta: Option[LoadMeta] = None, state:Option[LoadState] = None) {
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
  def isUp = meta.exists(_.floor == Floor.Upstairs)
  def isDown = meta.exists(_.floor == Floor.Downstairs)
  def withSetLevel(level: Int) = {
    val newState = state match {
      case None => LoadState(id, level, DateTime.now)
      case Some(st) => st.copy(level = level, timestamp = DateTime.now)
    }
    this.copy(state = Some(newState))
  }
  val longName = s"$areaName - $outputName"
}

object LuConfig extends Logging {
  val repeaterIpAddress = "192.168.1.2"
  lazy val defaultFetcher = XML.load(new URL(s"http://$repeaterIpAddress/DbXmlInfo.xml"))
  lazy val prodInstance = new LuConfig(defaultFetcher)
  val testConfig =
    <Config>
      <Area Name ="Kitchen">
        <Output Name ="Kitchen Island" IntegrationID="1"></Output>
        <Output Name ="Kitchen Cans" IntegrationID="2"></Output>
        <Output Name ="Kitchen Spots" IntegrationID="3"></Output>
      </Area>
      <Area Name ="Breakfast Room">
        <Output Name ="Breakfast Cans" IntegrationID="4"></Output>
        <Output Name ="Breakfast Sconces" IntegrationID="5"></Output>
        <Output Name ="Breakfast Chandelier" IntegrationID="6"></Output>
      </Area>
      <Area Name ="Master">
        <Output Name ="Master Cans" IntegrationID="7"></Output>
        <Output Name ="Master Reading lights" IntegrationID="8"></Output>
      </Area>
    </Config>
  val locInstance = new LuConfig(testConfig)

  lazy val snapshotFile = this.getClass.getResource("config.snapshot.xml")
  lazy val snapshotInstance = new LuConfig(XML.load(snapshotFile))

  def apply() = if(Settings().localOnly) {
    info("using local instance")
    locInstance
//    snapshotInstance
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
      val meta = MetaConfig().byId(idInt)
      LightingLoad(idInt, areaName.text, outputName.text, meta)
    }
    ll.sortBy(_.id).foreach(l => info(l.toString))
    LoadSet(ll.toSet)
  }

  def storedConfig = parseXmlOnce

  var parseXmlOnce = parseXml()

  def reload() = {
    parseXmlOnce = parseXml()
    parseXmlOnce
  }

}

object LuConfigTest extends App {
  LuConfig().parseXml
}
