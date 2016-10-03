package org.runger.lulight.rds

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}


/**
  * Created by randy on 9/29/16.
  */

object BootstrapIds {
  val ungerProjectId = UUID.fromString("494e8b61-e98d-4dbe-ae8b-c63a03afd8da")

  //Groups
  val brkRmGroupId = UUID.fromString("111e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val cansGroupId = UUID.fromString("222e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val allGroupId = UUID.fromString("333e8b61-e98d-4dbe-ae8b-c63a03afd8da")

  //To test subgroups
  val super1Id = UUID.fromString("011e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val super2Id = UUID.fromString("022e8b61-e98d-4dbe-ae8b-c63a03afd8da")

  //Lights
  val famCanId = UUID.fromString("444e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val brkCanId = UUID.fromString("555e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val brkSconceId = UUID.fromString("666e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val outdoorLightId = UUID.fromString("777e8b61-e98d-4dbe-ae8b-c63a03afd8da")
}

object BootstrapData extends App {
  import BootstrapIds._
  import RDSConfig.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  val db = Database.forURL("jdbc:postgresql://black-pearl:5432/", driver = "org.postgresql.Driver", user="postgres")

  createProjects()
  createGroups()
  createLoads()

  createAssoc()

  def createAssoc(): Unit ={
    val assocLoad = TableQuery[LoadGroupAssocLoadTable]
    val assocGroup = TableQuery[LoadGroupAssocSubGroupTable]

    //Cans assoc
    val brkCanAssoc = LoadGroupAssocLoadRow(cansGroupId, brkCanId)
    val famCanAssoc = LoadGroupAssocLoadRow(cansGroupId, famCanId)

    //Brk assoc
    val brkCanAssoc2 = LoadGroupAssocLoadRow(brkRmGroupId, brkCanId)
    val brkSconce = LoadGroupAssocLoadRow(brkRmGroupId, brkSconceId)

    //All assoc - cans + brk + single outdoor
    val allAssocCans = LoadGroupAssocSubGroupRow(allGroupId, cansGroupId)
    val allAssocBrk = LoadGroupAssocSubGroupRow(allGroupId, brkRmGroupId)
    val allAssocOut = LoadGroupAssocLoadRow(allGroupId, outdoorLightId)

    //Super assocs
    val super1Assoc = LoadGroupAssocSubGroupRow(super1Id, allGroupId)
    val super2Assoc = LoadGroupAssocSubGroupRow(super2Id, super1Id)


    createInTransaction("Create assoc Load table", assocLoad.schema.create)
    createInTransaction("Create assoc Subgroup table", assocGroup.schema.create)

    insertInTransaction("Populate assoc table",
      //Put cans in can group
      assocLoad += brkCanAssoc
      , assocLoad += famCanAssoc
      //Put breakfast in brk group
      , assocLoad += brkCanAssoc2
      , assocLoad += brkSconce
      //Put both above groups, plus outdoor light in All group
      , assocGroup += allAssocCans
      , assocGroup += allAssocBrk
      , assocLoad += allAssocOut

      , assocGroup += super1Assoc
      , assocGroup += super2Assoc

      , assocLoad.result.map(println)
    )
  }

  def createLoads(): Unit = {
    val loads = TableQuery[LoadTable]

    val brkCansLoad = LoadRow(brkCanId, ungerProjectId, 10, "Breakfast Room Cans", "Breakfast Room", "Cans", "pub", "led")
    val brkSconceLoad = LoadRow(brkSconceId, ungerProjectId, 11, "Breakfast sconces", "Living Room", "Cans", "pub", "led")
    val famCansLoad = LoadRow(famCanId, ungerProjectId, 12, "Fam Room Cans", "Living Room", "Cans", "pub", "led")
    val outdoorLoad = LoadRow(outdoorLightId, ungerProjectId, 13, "Outdoor light", "Living Room", "Cans", "pub", "led")

    createInTransaction("Create load table", loads.schema.create)

    insertInTransaction("Populate loads table",
      loads += brkCansLoad
      , loads += brkSconceLoad
      , loads += famCansLoad
      , loads += outdoorLoad
      , loads.result.map(println)
    )

  }

  def createGroups(): Unit = {
    val groups = TableQuery[LoadGroupTable]

    val brkRoom = LoadGroupRow(brkRmGroupId, ungerProjectId, "Breakfast Room")
    val allCans = LoadGroupRow(cansGroupId, ungerProjectId, "All Cans")
    val all = LoadGroupRow(allGroupId, ungerProjectId, "All")

    //Subgroups
    val super1grp = LoadGroupRow(super1Id, ungerProjectId, "Super1")
    val super2grp = LoadGroupRow(super2Id, ungerProjectId, "Super2")

    createInTransaction("Create group table", groups.schema.create)

    insertInTransaction("Populate group table",
      groups += brkRoom
      , groups += allCans
      , groups += all

      //Test subgroups
      , groups += super1grp
      , groups += super2grp

      , groups.result.map(println)
    )
  }


  def createProjects(): Unit = {
    val projects = TableQuery[ProjectTable]
    val proj1 = ProjectRow(ungerProjectId, "Unger House")

    createInTransaction("Create Project table", projects.schema.create)

    insertInTransaction("Populate Unger project",
      projects += proj1
      , projects.result.map(println)
    )
  }

  def createInTransaction[A](stepName: String, q: DBIOAction[A, NoStream, Nothing]): Unit = {
    println(s"\n\n$stepName: ")

    try {
      Await.result(db.run(
        q
      ), Duration.Inf)
    } catch {
      case ex: Exception if ex.getMessage.contains("already exists") => println(s" already exists")
      case ex => ex.printStackTrace()
    }
  }

  def insertInTransaction[A](stepName: String, rows: DBIOAction[A, NoStream, Nothing]*): Unit = {
    println(s"$stepName: ")

    rows.foreach(row => {
      try {
        Await.result(db.run(
          row
        ), Duration.Inf)
      } catch {
        case ex: Exception if ex.getMessage.contains("already exists") => println(s" already exists")
        case ex => ex.printStackTrace()
      }
    })
  }

}

object TestQueries extends App {
  import BootstrapIds._
  import RDSConfig.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  val db = Database.forURL("jdbc:postgresql://black-pearl:5432/", driver = "org.postgresql.Driver", user="postgres")
  val projects = TableQuery[ProjectTable]
  val loads = TableQuery[LoadTable]
  val groups = TableQuery[LoadGroupTable]
  val assocs = TableQuery[LoadGroupAssocLoadTable]

  val res = Await.result(db.run(
    findGroupsWithLoads().result.head.map{ case (gId, dName, lId, lName, lBulb) => {
      println(s"($gId, $dName, $lId, $lName, $lBulb)")
    }}
  ), Duration.Inf)

  def findGroupsWithLoads() ={
    for {
      group <- groups
      if group.projectId === ungerProjectId
      assoc <- assocs
      if assoc.hostGroupId === group.id
      load <- loads
      if assoc.loadId === load.id
    } yield (group.id, group.name, load.id, load.displayName, load.bulbType)
  }

  def findLeds(): Unit = {
    val res = Await.result(db.run(
      loads.filter(_.bulbType === "led").result.map(r => r.foreach(println))
    ), Duration.Inf)
  }


}
