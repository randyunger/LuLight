package org.runger.lulight.rds

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}


/**
  * Created by randy on 9/29/16.
  */

object BootstrapIds {
  val projectId = UUID.fromString("494e8b61-e98d-4dbe-ae8b-c63a03afd8da")

  //Groups
  val brkRmGroupId = UUID.fromString("111e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val cansGroupId = UUID.fromString("222e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val allGroupId = UUID.fromString("333e8b61-e98d-4dbe-ae8b-c63a03afd8da")

  //Lights
  val famCanId = UUID.fromString("444e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val brkCanId = UUID.fromString("555e8b61-e98d-4dbe-ae8b-c63a03afd8da")
  val brkSconceId = UUID.fromString("666e8b61-e98d-4dbe-ae8b-c63a03afd8da")
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
    val assoc = TableQuery[LoadGroupAssocLoadTable]

    //Cans assoc
    val brkCanAssoc = LoadGroupAssocLoadRow(cansGroupId, Some(brkCanId), None)
    val famCanAssoc = LoadGroupAssocLoadRow(cansGroupId, Some(famCanId), None)

    //Brk assoc
    val brkCanAssoc2 = LoadGroupAssocLoadRow(brkRmGroupId, Some(brkCanId), None)
    val brkSconce = LoadGroupAssocLoadRow(brkRmGroupId, Some(brkSconceId), None)

    //All assoc - cans + brk
    val allAssocCans = LoadGroupAssocLoadRow(allGroupId, None, Some(cansGroupId))
    val allAssocBrk = LoadGroupAssocLoadRow(allGroupId, None, Some(brkRmGroupId))

    createInTransaction("Create assoc table", assoc.schema.create)

    insertInTransaction("Populate assoc table",
      assoc += brkCanAssoc
      , assoc += famCanAssoc
      , assoc += brkCanAssoc2
      , assoc += brkSconce
      , assoc += allAssocCans
      , assoc += allAssocBrk
      , assoc.result.map(println)
    )
  }

  def createLoads(): Unit = {
    val loads = TableQuery[LoadTable]

    val brkCansLoad = LoadRow(brkCanId, projectId, 10, "Breakfast Room Cans", "Breakfast Room", "Cans", "pub", "led")
    val brkSconceLoad = LoadRow(brkSconceId, projectId, 11, "Breakfast sconces", "Living Room", "Cans", "pub", "led")
    val famCansLoad = LoadRow(famCanId, projectId, 12, "Fam Room Cans", "Living Room", "Cans", "pub", "led")

    createInTransaction("Create load table", loads.schema.create)

    insertInTransaction("Populate loads table",
      loads += brkCansLoad
      , loads += brkSconceLoad
      , loads += famCansLoad
      , loads.result.map(println)
    )

  }

  def createGroups(): Unit = {
    val groups = TableQuery[LoadGroupTable]

    val brkRoom = LoadGroupRow(brkRmGroupId, projectId, "Breakfast Room")
    val allCans = LoadGroupRow(cansGroupId, projectId, "All Cans")
    val all = LoadGroupRow(allGroupId, projectId, "All")

    createInTransaction("Create group table", groups.schema.create)

    insertInTransaction("Populate group table",
      groups += brkRoom
      , groups += allCans
      , groups += all
      , groups.result.map(println)
    )
  }


  def createProjects(): Unit = {
    val projects = TableQuery[ProjectTable]
    val proj1 = ProjectRow(projectId, "Unger House")

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
