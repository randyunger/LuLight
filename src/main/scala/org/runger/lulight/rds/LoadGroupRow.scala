package org.runger.lulight.rds

import java.util.UUID

import org.runger.lulight.Logging
import org.runger.lulight.rds.RDSConfig.api._

/**
  * Created by randy on 9/29/16.
  */
case class LoadGroupRow (id: UUID, name: String)

class LoadGroupTable(tag: Tag) extends Table[LoadGroupRow](tag, "LoadGroups") with Logging {

  def id = column[UUID]("id", O.PrimaryKey)
  def name = column[String]("name")

  val toRow = (tup: (UUID, String)) => {
    LoadGroupRow(tup._1, tup._2)
  }

  val fromRow = (row: LoadGroupRow) => {
    Option((row.id, row.name))
  }

  override def * = (id, name) <> (toRow, fromRow)
}

