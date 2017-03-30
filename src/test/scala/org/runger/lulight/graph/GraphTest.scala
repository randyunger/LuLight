package org.runger.lulight.graph

//import java.io.File
//import java.util.UUID
//
//import org.neo4j.graphdb.Node
//import org.neo4j.graphdb.factory.GraphDatabaseFactory
//import org.specs2.mutable.Specification
//
//import scala.collection.JavaConversions._

/**
  * Created by randy on 10/5/16.
  */


//class GraphTest extends Specification {
//  "Graph" should {
//    "create" in {
//      Graph("/opt/neo4j/deviceGraphTest")
//
//      ok
//    }
//
//    def printNode(node: Node): Unit = {
//      val props = for {
//        prop <- node.getAllProperties.toList
//        (k, v) = prop
//      } yield (k,v)
//      println(s"Node ${node.getId} :: " + props.mkString("::"))
//    }
//
//    "populate" in {
//      val graph = Graph("/opt/neo4j/deviceGraphTest")
//      val graphDb = graph.graphDb
//      val tx = graphDb.beginTx()
//      val node = graphDb.createNode()
//      node.setProperty("name", "kitchen island")
//      node.setProperty("bulbType", "incandescent")
//
//      val allNodes = graphDb.getAllNodes.iterator().toList
//      allNodes.foreach(node => {
//        printNode(node)
//      })
//      tx.success()
//      graphDb.shutdown()
//      ok
//    }
//
//    "read" in {
//      val graph = Graph("/opt/neo4j/deviceGraphTest")
//      val graphDb = graph.graphDb
//      graphDb.beginTx()
//      val allNodes = graphDb.getAllNodes.iterator().toList
//      allNodes.foreach(node => {
//        printNode(node)
//      })
//      ok
//    }
//
//    "r/w" in {
//      val graph1 = {
//        val graphDb = new GraphDatabaseFactory()
//          .newEmbeddedDatabase(new File("/opt/neo4j/deviceGraphTest" ))
//        val tx = graphDb.beginTx()
//        val node = graphDb.createNode()
//        node.setProperty("name", "kitchen island")
//        node.setProperty("bulbType", "incandescent")
//        tx.success()
//        tx.close()
//        graphDb.shutdown()
//      }
//
//      val graph2 = {
//        val graphDb2 = new GraphDatabaseFactory()
//          .newEmbeddedDatabase(new File("/opt/neo4j/deviceGraphTest" ))
//        val tx2 = graphDb2.beginTx()
//        val allNodes = graphDb2.getAllNodes.iterator().toList
//        allNodes.foreach(node => {
//          printNode(node)
//        })
//      }
//
//      ok
//    }
//
//
//
//    "bootStrap into graph" in {
////      LoadRow(id: UUID, projectId: UUID, localId: Int,  displayName: String, roomName: String,
////        fixtureType: String, privacyStatus: String, bulbType: String)
//      val brkCanId = UUID.fromString("555e8b61-e98d-4dbe-ae8b-c63a03afd8da")
//      val brkCansLoad = LoadNode(brkCanId.toString, 10, "Breakfast Room Cans", "Breakfast Room", "Cans", "pub", "led")
//      val loadList = List(brkCansLoad)
//
//      val graph = Graph("/opt/neo4j/deviceGraphTest")
//      val graphDb = graph.graphDb
//      graphDb.beginTx()
//      val tx = graphDb.beginTx()
//
//      loadList.foreach(loadNode => {
//        graph.persist(loadNode)
//      })
//
//
//      tx.success()
//      tx.close()
//
//      graph.printString()
//
//      graphDb.shutdown()
//
//      ok
//    }
//
//  }
//}
