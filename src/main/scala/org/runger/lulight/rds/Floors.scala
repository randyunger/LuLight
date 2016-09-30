//package org.runger.lulight.rds
//
///**
//  * Created by randy on 9/26/16.
//  */
//
//import java.util.UUID
//
//import org.runger.lulight.Logging
//import org.runger.lulight.rds.RDSConfig.api._
//
//case class FloorRow(id: UUID, name: String)
//
//class FloorTable(tag: Tag) extends Table[FloorRow](tag, "FLOOR") with Logging {
//  def id = column[String]("id", O.PrimaryKey)
//  def name = column[String]("name")
//
//  val toRow = (tup: (String, String)) => {
//    FloorRow(UUID.fromString(tup._1), tup._2)
//  }
//
//  val fromRow = (row: FloorRow) => {
//    Some((row.id.toString, row.name))
//  }
//
//  override def * = (id, name) <> (toRow, fromRow)
//}
