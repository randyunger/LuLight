package org.runger.lulight.rds

import java.util.UUID

import org.runger.lulight.Logging
import org.runger.lulight.rds.RDSConfig.api._

/**
  * Created by randy on 9/29/16.
  */

/*
Project is a special case of load group. Should we model that better?
Table inheritance?
 */

case class ProjectRow(id: UUID, name: String)

class ProjectTable(tag: Tag) extends Table[ProjectRow](tag, "Projects") with Logging {

  def id = column[UUID]("id", O.PrimaryKey)
  def name = column[String]("name")

  val toRow = (tup: (UUID, String)) => {
    ProjectRow(tup._1, tup._2)
  }

  val fromRow = (row: ProjectRow) => {
    Some((row.id, row.name))
  }

  override def * = (id, name) <> (toRow, fromRow)
}

