package org.runger.lulight.rds

/**
  * Created by randy on 9/26/16.
  */

import java.util.UUID

import org.runger.lulight.Logging
import org.runger.lulight.rds.RDSConfig.api._

case class LoadRow(id: UUID, projectId: UUID, localId: Int,  displayName: String, roomName: String,
                   fixtureType: String, privacyStatus: String, bulbType: String)

object LoadTable {
  val locations = TableQuery[ProjectTable]
}

class LoadTable(tag: Tag) extends Table[LoadRow](tag, "Loads") with Logging {

  def id = column[UUID]("id", O.PrimaryKey)
  def projectId = column[UUID]("projectId")
  def project = foreignKey("locationId", projectId, LoadTable.locations)(_.id, ForeignKeyAction.Restrict)
  def localId = column[Int]("localId")
  def displayName = column[String]("displayName")

  def roomName = column[String]("roomName") //todo Do we need rooom name in light of groups
  def privacyStatus = column[String]("privacyStatus")
  def fixtureType = column[String]("fixtureType")
  def bulbType = column[String]("bulbType")

  val toRow = (tup: (UUID, UUID, Int, String, String, String, String, String)) => {
    LoadRow(tup._1, tup._2, tup._3, tup._4, tup._5, tup._6, tup._7, tup._8)
  }

  val fromRow = (row: LoadRow) => {
    Some((row.id, row.projectId, row.localId, row.displayName, row.roomName, row.fixtureType, row.privacyStatus, row.bulbType))
  }

  override def * = (id, projectId, localId, displayName, roomName, fixtureType, privacyStatus, bulbType) <> (toRow, fromRow)
}
