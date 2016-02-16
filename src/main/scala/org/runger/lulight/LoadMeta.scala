package org.runger.lulight

import play.api.libs.json.Json

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
  val LED = Type("l")
  val Incandescent = Type("i")
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

case class LoadMeta(luId: Int, name: String, floor: Floor.Type, shared: SharedStatus.Type, led: BulbType.Type, interior: IntExt.Type)

object LoadMeta {
  implicit val loadMetaFormat = Json.format[LoadMeta]
}

object MetaConfig extends Logging {
  val meta = Set(
    LoadMeta(65, "Garage Stairs", Floor.Garage, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior)
    , LoadMeta(1, "Kitchen Island", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior)
    , LoadMeta(2, "Kitchen Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior)
    , LoadMeta(3, "Kitchen Spots", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior)
    , LoadMeta(4, "Breakfast Cans", Floor.Downstairs, SharedStatus.Public, BulbType.LED, IntExt.Interior)
    , LoadMeta(5, "Breakfast Sconces", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior)
    , LoadMeta(6, "Breakfast Chandelier", Floor.Downstairs, SharedStatus.Public, BulbType.Incandescent, IntExt.Interior)
  )

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
