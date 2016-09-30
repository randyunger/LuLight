package org.runger.lulight.rds

import java.util.UUID

import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}


/**
  * Created by randy on 9/29/16.
  */
object BootstrapData extends App {
  import RDSConfig.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  val db = Database.forURL("jdbc:postgresql://black-pearl:5432/", driver = "org.postgresql.Driver", user="postgres")

  createProjects()

  def createLoads(): Unit = {
    val loads = TableQuery[LoadTable]

//    val load1 = LoadRow(UUID.randomUUID(), 10, "Living Room Cans", "Living Room", "Cans", "pub", "led")
//    val load2 = LoadRow(UUID.randomUUID(), 11, "Living Room Sconces", "Living Room", "Sconces", "pub", "inc")

  }

  def createProjects(): Unit = {
    val projects = TableQuery[ProjectTable]
    val proj1 = ProjectRow(UUID.fromString("494e8b61-e98d-4dbe-ae8b-c63a03afd8da"), "Unger House")

    try {
      Await.result(db.run(DBIO.seq(
        projects.schema.create
      )), Duration.Inf)
    } catch {
      case ex: Exception => ex.printStackTrace()
    }


    try {
      Await.result(db.run(DBIO.seq(
        projects += proj1
        , projects.result.map(println)
      )), Duration.Inf)
    } catch {
      case ex: Exception => ex.printStackTrace()
    }


  }


}
