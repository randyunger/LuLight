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
                      , ids: Set[Int] = Set.empty
                    ) {
  def filter(loadSet: LoadSet) = {
    val loads = loadSet.loads.filter(ll => {
      val matchesABulbType = filterB(ll)
      val matchesAnIntExt = filterI(ll)
      val matchesAFloor = filterF(ll)
      val matchesAnId = filterId(ll)
      val passesFilter = matchesABulbType && matchesAnIntExt && matchesAFloor && matchesAnId
      passesFilter
    })
    LoadSet(loads)
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

object MetaConfig extends Logging {
  val meta = mutable.Buffer.empty[LoadMeta]

  def addMeta(loadMeta: LoadMeta) = {
    meta += loadMeta
    loadMeta
  }

  val GarageStairs = addMeta(LoadMeta(65, "Garage Stairs", Floor.Garage, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val KitchenIsland =  addMeta(LoadMeta(1, "Kitchen Island", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val KitchenCans = addMeta(LoadMeta(2, "Kitchen Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val KitchenSpots = addMeta(LoadMeta(3, "Kitchen Spots", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val BreakfastCans = addMeta(LoadMeta(4, "Breakfast Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior))
  val BreakfastSconces = addMeta(LoadMeta(5, "Breakfast Sconces", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val BreakfastChandi = addMeta(LoadMeta(6, "Breakfast Chandelier", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior))
  val MasterCans = addMeta(LoadMeta(7, "Master Cans", Floor.Upstairs, SharedStatus.Private, BulbType.LED, IntExt.Interior))
  val MasterReading = addMeta(LoadMeta(8, "Master Reading Lights", Floor.Upstairs, SharedStatus.Private, BulbType.Incandescent, IntExt.Interior))


  def byId(id: Int) = {
    meta.find(_.luId == id)
  }
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
