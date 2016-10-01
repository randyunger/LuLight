package org.runger.lulight.rds

/**
  * Created by randy on 9/30/16.
  */
import java.util.UUID
import org.runger.lulight.Logging
import org.runger.lulight.rds.RDSConfig.api._

case class LoadGroupAssocSubGroupRow(hostGroupId: UUID, subGroupId: UUID)

class LoadGroupAssocSubGroupTable(tag: Tag) extends Table[LoadGroupAssocSubGroupRow](tag, "LoadGroupAssocSubGroup") with Logging {

  def hostGroupId = column[UUID]("hostGroupId")
  def hostGroup = foreignKey("hostGroupId", hostGroupId, Tables.groups)(_.id, ForeignKeyAction.Restrict)

  def subGroupId = column[UUID]("subGroupId")
  def subGroup = foreignKey("subGroupId", subGroupId, Tables.groups)(_.id, ForeignKeyAction.Restrict)

  def uniqueHostLoad = index("uniqueHostLoadSub", (hostGroupId, subGroupId), unique = true)

  val toRow = (tup: (UUID, UUID)) => {
    LoadGroupAssocSubGroupRow(tup._1, tup._2)
  }

  val fromRow = (row: LoadGroupAssocSubGroupRow) => {
    Option((row.hostGroupId, row.subGroupId))
  }

  override def * = (hostGroupId, subGroupId) <> (toRow, fromRow)
}
