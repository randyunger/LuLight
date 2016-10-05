package org.runger.lulight.graph

import java.io.File

import org.neo4j.cypher.internal.ExecutionEngine
import org.neo4j.graphdb.{Label, Path, RelationshipType}
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.traversal.{Evaluation, Evaluator}
import org.specs2.mutable.Specification

import scala.collection.JavaConverters._

/**
  * Created by runger on 10/3/16.
  */
class EmbeddedNeoTest extends Specification {
  "embedded neo" should {
    "new db" in {
      val location = "/opt/neo4j/movieExample"
      val file = new File(location)
      //Clear out directory/empty database
      file.listFiles().foreach(f => f.delete())

      val params: java.util.Map[String, String] = new java.util.HashMap[String, String]
      params.put("allow_store_upgrade", "true")
      params.put("neostore.nodestore.db.mapped_memory", "100M")
      params.put("neostore.relationshipstore.db.mapped_memory", "700M")
      params.put("neostore.propertystore.db.mapped_memory", "1400M")
      params.put("neostore.propertystore.db.strings.mapped_memory", "1500M")
      params.put("neostore.propertystore.db.arrays.mapped_memory", "1M")

      val graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(file)

      val tx = graphDb.beginTx()

      val person = new Label {
        val name = "Person"
      }

      val movie = new Label {
        val name = "Movie"
      }

      val actedIn = new RelationshipType{
        val name = "ActedIn"
      }

      val silence = graphDb.createNode(movie)
      silence.setProperty("Title", "The Silence of the Lambs")

      val mjb = graphDb.createNode(movie)
      mjb.setProperty("Title", "Meet Joe Black")

      val seven = graphDb.createNode(movie)
      seven.setProperty("Title", "Se7en")

      val anthonyHopkins = graphDb.createNode(person)
      anthonyHopkins.setProperty("Name", "Anthony Hopkins")
      anthonyHopkins.createRelationshipTo(silence, actedIn)
      anthonyHopkins.createRelationshipTo(mjb, actedIn)

      val jodieFoster = graphDb.createNode(person)
      jodieFoster.setProperty("Name", "Jodie Foster")
      jodieFoster.createRelationshipTo(silence, actedIn)

      val brad = graphDb.createNode(person)
      brad.setProperty("Name", "Brad Pit")
      brad.createRelationshipTo(mjb, actedIn)
      brad.createRelationshipTo(seven, actedIn)

      val morg = graphDb.createNode(person)
      morg.setProperty("Name", "Morgan Freeman")
      morg.createRelationshipTo(seven, actedIn)

      val eval = new Evaluator {
        override def evaluate(path: Path): Evaluation = {
          if(path.length() == 2) Evaluation.INCLUDE_AND_PRUNE
          else Evaluation.EXCLUDE_AND_PRUNE
//          else
//          if(path.endNode().hasLabel(person)) Evaluation.INCLUDE_AND_CONTINUE
//          else Evaluation.EXCLUDE_AND_PRUNE
        }
      }

      val res = graphDb.traversalDescription()
        .breadthFirst()
        .evaluator(eval)
        .relationships(actedIn)
        .traverse(anthonyHopkins)

      val sRes = res.nodes().asScala

      sRes.foreach(node => {
        val props = node.getAllProperties.asScala
        val pStr = props.mkString("::")
        println(s"${node.getLabels.asScala.mkString("::")} $pStr")
      })

//      println("Here's the same output via Cypher:\n")

      def queryByName(actorName: String) = {
        val query = s"""
                      |MATCH (p: Person {Name:"$actorName"}) - [:ActedIn] -> (m:Movie) <- [:ActedIn] - (coActor: Person)
                      |RETURN m.Title, coActor.Name
                    """.stripMargin

        val cRes = graphDb.execute(query)

        //      cRes.asScala.foreach(um => um.asScala.foreach{case (k,v) => {
        //        println(s"$k $v")
        //      }})

        println(s"$actorName costars:")

        cRes.asScala.foreach(resMapJ => {
          val resMap = resMapJ.asScala
          val coStar = resMap.getOrElse("coActor.Name","")
          val movieTitle = resMap.getOrElse("m.Title","")
          println(s"$coStar in $movieTitle")
        })
      }

      queryByName("Brad Pit")

      tx.success()
      tx.close()

      ok
    }
  }
}
