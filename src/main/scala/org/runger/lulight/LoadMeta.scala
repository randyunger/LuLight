package org.runger.lulight

import play.api.libs.json.Json

import scala.collection.mutable

/**
  *
  * Created by Unger on 2/15/16.
  *
  **/

object Floor {
//  trait Type {
//    def name: String
//    def ix: Int
//  }
//  object Type {
//    def mkType(n: String, i: Int) = new Type {
//      override def name: String = n
//      override def ix: Int = i
//    }
//  }
//  val Garage = Type.mkType("Garage", -1)
//  val Downstairs = Type.mkType("Downstairs", 0)
//  val Upstairs = Type.mkType("Upstairs", 1)
  case class Type(code: String)
  implicit val typeFormat = Json.format[Type]
  val Garage = Type("G")
  val Downstairs = Type("D")
  val Upstairs = Type("U")
  //todo: How does exterior fit in here?
}

object SharedStatus {
  case class Type(code: String)
  implicit val typeFormat = Json.format[Type]
  val Public = Type("p")
  val Private = Type("v")
}

object BulbType {
  case class Type(code: String)
  implicit val typeFormat = Json.format[Type]
  val LED = Type("LED")
  val Incandescent = Type("Incandescent")
//  val Fan = Type("Fan") //todo: Fan

//  trait Type
//  object LED extends Type
//  object Incandescent extends Type
}

object IntExt {
  case class Type(code: String)
  implicit val typeFormat = Json.format[Type]
  val Interior = Type("i")
  val Exterior = Type("e")
//  trait Type
//  object Interior extends Type
//  object Exterior extends Type
}

case class FilterSet(
                      intExts: Set[IntExt.Type] = Set.empty
                      , bulbTypes: Set[BulbType.Type] = Set.empty
                      , floors: Set[Floor.Type] = Set.empty
                      , shareTypes: Set[SharedStatus.Type] = Set.empty
                      , ids: Set[Int] = Set.empty
                    ) {
  def filter(loadSet: LoadSet) = {
    val loads = loadSet.loads.filter(ll => {
      val matchesABulbType = filterB(ll)
      val matchesAnIntExt = filterI(ll)
      val matchesAFloor = filterF(ll)
      val matchesAShareStatus = filterS(ll)
      val matchesAnId = filterId(ll)

      val passesFilter = matchesABulbType && matchesAnIntExt && matchesAFloor && matchesAShareStatus && matchesAnId
      passesFilter
    })
    LoadSet(loads)
  }

  def filterS(load: LightingLoad) = {
    shareTypes.isEmpty || load.meta.exists(m => shareTypes.contains(m.shared))
  }

  def filterB(load: LightingLoad) = {
    bulbTypes.isEmpty || load.meta.exists(m => bulbTypes.contains(m.bulb))
  }

  def filterI(load: LightingLoad) = {
    intExts.isEmpty || load.meta.exists(m => intExts.contains(m.intExt))
  }

  def filterF(load: LightingLoad) = {
    floors.isEmpty || load.meta.exists(m => floors.contains(m.floor))
  }

  def filterId(load: LightingLoad) = {
    ids.isEmpty || ids.contains(load.id)
  }

//  def filterX(load: LightingLoad) = {
//    load.meta.exists(m => bulbTypes.contains(m.bulb))
//  }
}

object FilterSet {
  implicit val filterSetJson = Json.format[FilterSet]
}

case class LoadMeta(luId: Int, name: String, floor: Floor.Type, shared: SharedStatus.Type, bulb: BulbType.Type, intExt: IntExt.Type)

object LoadMeta {
  implicit val loadMetaFormat = Json.format[LoadMeta]
}

object MetaConfig {
  val prodInstance = new MetaConfig
  def apply() = prodInstance

  val localInstance = new LocalMetaConfig
}

trait MetaConfigBase {
  val meta = mutable.Buffer.empty[LoadMeta]

  def addMeta(loadMeta: LoadMeta) = {
    meta += loadMeta
    loadMeta
  }

  def byId(id: Int) = {
    meta.find(_.luId == id)
  }
}

class MetaConfig extends MetaConfigBase with Logging {
  val FamilyRoomFront = addMeta(LoadMeta(23, "Family Room Front", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val BackYardLV = addMeta(LoadMeta(24, "Backyard Low Voltage", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Exterior))
  val ReedCans = addMeta(LoadMeta(25, "Reed's Bedroom Cans", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val ReedDesk = addMeta(LoadMeta(26, "Reed's Desk Cans", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val PaigeCans = addMeta(LoadMeta(27, "Paige's Bedroom Cans", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val ReedFan = addMeta(LoadMeta(28, "Reed's Bedroom Fan", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val BreakfastCans = addMeta(LoadMeta(29, "Breakfast Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val BreakfastSconces = addMeta(LoadMeta(30, "Breakfast Sconces", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val BreakfastChandi = addMeta(LoadMeta(31, "Breakfast Chandelier", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val BackyardCourtyard = addMeta(LoadMeta(32, "Backyard Courtyard", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Exterior))
  val BackyardPlanter = addMeta(LoadMeta(33, "Backyard Planter", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Exterior))
  val BackyardPatio = addMeta(LoadMeta(34, "Backyard Patio", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Exterior))
  val BackyardString = addMeta(LoadMeta(35, "Backyard String Lights", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Exterior))
  val WestSide = addMeta(LoadMeta(36, "West side lights", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Exterior))

  val FamilyRoomBack = addMeta(LoadMeta(42, "Family Room Back", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val BalconyExterior = addMeta(LoadMeta(45, "Balcony Exterior", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Exterior))
  val GarageExterior = addMeta(LoadMeta(47, "Garage Exterior", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Exterior))
  val FrontLV = addMeta(LoadMeta(48, "Front Yard Low Voltage", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Exterior))
  val PlayRoomOuter = addMeta(LoadMeta(49, "Playroom Outer", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val PlayRoomInner = addMeta(LoadMeta(50, "Playroom Inner", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val GuestCans = addMeta(LoadMeta(52, "Guest Bedroom Cans", Floor.Downstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val GuestFan = addMeta(LoadMeta(53, "Guest Bedroom Fan", Floor.Downstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val KitchenCans = addMeta(LoadMeta(54, "Kitchen Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val KitchenSpots = addMeta(LoadMeta(55, "Kitchen Spot lights", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val KitchenIslandPendants = addMeta(LoadMeta(56, "Kitchen Island Pendants", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))

  val OfficeCans = addMeta(LoadMeta(60, "Office Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val OfficeSconces = addMeta(LoadMeta(61, "Office Sconces", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val OfficePendant = addMeta(LoadMeta(62, "Office Pendant", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))

  val KitchenStairs = addMeta(LoadMeta(65, "Kitchen Stairs", Floor.Garage, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val VestibulePendant = addMeta(LoadMeta(66, "Vestibule Pendant", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val DiningCans = addMeta(LoadMeta(67, "Dining Room Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val DiningSconces = addMeta(LoadMeta(68, "Dining Room Sconces", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val DiningChandi = addMeta(LoadMeta(69, "Dining Room Chandelier", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))

  val KitchenCabinets= addMeta(LoadMeta(71, "Kitchen Cabinets", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val KitchenStairs2 = addMeta(LoadMeta(72, "XX Kitchen Stairs?", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val FoyerPendant = addMeta(LoadMeta(73, "Foyer Pendant", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))

  val UpstairsHallway = addMeta(LoadMeta(78, "Upstairs Hallway", Floor.Upstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val UpstairsStairs = addMeta(LoadMeta(79, "Upstairs stairs", Floor.Upstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val PaigeBedroomFan = addMeta(LoadMeta(81, "Paige's Bedroom Fan", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val PaigeDesk = addMeta(LoadMeta(82, "Paige's Bedroom Desk", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val UpstairsDenCans = addMeta(LoadMeta(83, "Den Cans", Floor.Upstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val DenSconces = addMeta(LoadMeta(84, "Den Sconces", Floor.Upstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val MasterFan = addMeta(LoadMeta(85, "Master Fan", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val MasterCansFront = addMeta(LoadMeta(87, "Master Cans Front", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val MasterCansBack = addMeta(LoadMeta(88, "Master Cans Back", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val MasterLara = addMeta(LoadMeta(90, "Lara's reading light", Floor.Upstairs, SharedStatus.Private, BulbType.Incandescent, IntExt.Interior))
  val MasterExterior = addMeta(LoadMeta(91, "Master Bedroom Exterior", Floor.Upstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Exterior))
  val MasterBathSconces = addMeta(LoadMeta(93, "Master Bath Sconces", Floor.Upstairs, SharedStatus.Private, BulbType.Incandescent, IntExt.Interior))
  val MasterBathPendant = addMeta(LoadMeta(94, "Master Bath Pendant", Floor.Upstairs, SharedStatus.Private, BulbType.Incandescent, IntExt.Interior))
  val MasterBathCans = addMeta(LoadMeta(95, "Master Bath Cans", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val MasterShowerCans = addMeta(LoadMeta(96, "Master Shower Cans", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val MasterToilet = addMeta(LoadMeta(97, "Master Toilet", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val MasterRandy = addMeta(LoadMeta(98, "Randy's reading light", Floor.Upstairs, SharedStatus.Private, BulbType.Incandescent, IntExt.Interior))

}

class LocalMetaConfig extends MetaConfigBase with Logging {
  val GarageStairs = addMeta(LoadMeta(65, "Garage Stairs", Floor.Garage, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val KitchenIsland =  addMeta(LoadMeta(1, "Kitchen Island", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val KitchenCans = addMeta(LoadMeta(2, "Kitchen Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val KitchenSpots = addMeta(LoadMeta(3, "Kitchen Spots", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val BreakfastCans = addMeta(LoadMeta(4, "Breakfast Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val BreakfastSconces = addMeta(LoadMeta(5, "Breakfast Sconces", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val BreakfastChandi = addMeta(LoadMeta(6, "Breakfast Chandelier", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val MasterCans = addMeta(LoadMeta(7, "Master Cans", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val MasterReading = addMeta(LoadMeta(8, "Master Reading Lights", Floor.Upstairs, SharedStatus.Private, BulbType.Incandescent, IntExt.Interior))

}

//object MetaConfig extends Logging {
//  val public = true
//  val priv = false
//  val led = true
//  val inc = false
//  val interior = true
//  val exterior = false
//
//  val meta = Set(
//    LoadMeta(0, "Nothing", -1, priv, led, interior)
//    ,LoadMeta(65, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(56, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(55, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(72, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(54, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(48, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(50, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(49, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(95, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(93, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(94, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(96, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//    ,LoadMeta(71, "Garage Stairs", -1, public, led, interior)
//
//  )
//
//
//}
