package org.runger.lulight.rds

import java.util.UUID

import org.specs2.mutable.Specification
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, _}
/**
  * Created by randy on 9/26/16.
  */
class RDSConfigTest extends Specification {

  "DB" should {
    "allow H2" in {
      import RDSConfig.api._
      import scala.concurrent.ExecutionContext.Implicits.global
      val db = Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
      val loads = TableQuery[LoadTable]

      val load1 = LoadRow(UUID.randomUUID(), UUID.randomUUID(), 10, "Living Room Cans", "Living Room", "Cans", "pub", "led")
      val load2 = LoadRow(UUID.randomUUID(), UUID.randomUUID(), 11, "Living Room Sconces", "Living Room", "Sconces", "pub", "inc")

      try {
        Await.result(db.run(DBIO.seq(
          loads.schema.create

          , loads += load1
          , loads += load2
          //        , org += OrganizationRow()

          , loads.result.map(println)

        )), Duration.Inf)
      } catch {
        case ex: Exception => ex.printStackTrace()
      }

      val tables = Await.result(db.run(MTable.getTables), 1.seconds).toList
      println(tables)
      tables.foreach(println)

      ok
    }

    "allow PostGres" in {
//      import RDSConfig.api._
      import RDSConfig.api._
      import scala.concurrent.ExecutionContext.Implicits.global
      val db = Database.forURL("jdbc:postgresql://black-pearl:5432/", driver = "org.postgresql.Driver", user="postgres")
      val loads = TableQuery[LoadTable]

      val load1 = LoadRow(UUID.randomUUID(), UUID.randomUUID(), 10, "Living Room Cans", "Living Room", "Cans", "pub", "led")
      val load2 = LoadRow(UUID.randomUUID(), UUID.randomUUID(), 11, "Living Room Sconces", "Living Room", "Sconces", "pub", "inc")

      try {
        Await.result(db.run(DBIO.seq(
          loads.schema.create

          , loads += load1
          , loads += load2
//          , loads.filter(_.bulbType == "led").result
          //        , org += OrganizationRow()

          , loads.result.map(println)

        )), Duration.Inf)
      } catch {
        case ex: Exception => ex.printStackTrace()
      }

      val tables = Await.result(db.run(MTable.getTables), 1.seconds).toList
      println(tables)
      tables.foreach(println)

      ok
    }


  }


}
