package org.runger.lulight.rds

import java.util.UUID

import org.runger.lulight.Logging
import org.runger.lulight.rds.RDSConfig.api._

/**
  * Created by randy on 9/29/16.
  */
case class LoadGroupRow (id: UUID, projectId: UUID, name: String)

class LoadGroupTable(tag: Tag) extends Table[LoadGroupRow](tag, "LoadGroups") with Logging {

  def id = column[UUID]("id", O.PrimaryKey)
  def projectId = column[UUID]("projectId")
  def project = foreignKey("projectId", projectId, Tables.projects)(_.id, ForeignKeyAction.Restrict)
  def name = column[String]("name")

  val toRow = (tup: (UUID, UUID, String)) => {
    LoadGroupRow(tup._1, tup._2, tup._3)
  }

  val fromRow = (row: LoadGroupRow) => {
    Option((row.id, row.projectId, row.name))
  }

  override def * = (id, projectId, name) <> (toRow, fromRow)
}

