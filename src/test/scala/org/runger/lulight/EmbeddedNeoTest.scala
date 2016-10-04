package org.runger.lulight

import java.io.File

import org.neo4j.cypher.internal.ExecutionEngine
import org.neo4j.graphdb.Label
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.traversal.TraversalContext
import org.specs2.mutable.Specification

import scala.collection.JavaConverters._


/**
  * Created by runger on 10/3/16.
  */
class EmbeddedNeoTest extends Specification {
  "embedded neo" should {
    "new db" in {
      val location = "/opt/neo4j"
      val file = new File(location)

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
      silence.setProperty("Title", "Meet Joe Black")

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

      val res = graphDb.traversalDescription()
        .breadthFirst()
        .relationships(actedIn)
        .traverse(anthonyHopkins)

      val sRes = res.nodes().asScala

      sRes.foreach(node => {
        val props = node.getAllProperties.asScala
        val pStr = props.mkString("::")
        println(pStr)
      })

      val engine = new ExecutionEngine(graphDb)

      tx.close()

      ok
    }
  }
}
