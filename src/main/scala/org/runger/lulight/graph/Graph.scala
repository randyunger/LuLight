package org.runger.lulight.graph

import java.io.File

import org.neo4j.graphdb.{Label, Node}
import org.neo4j.graphdb.factory.{GraphDatabaseFactory, GraphDatabaseSettings}

import scala.collection.JavaConversions._

/**
  * Created by randy on 10/5/16.
  */

object Graph {

  val deviceGraphPath = "/opt/neo4j/deviceGraph"
  val singleton = new Graph(deviceGraphPath)

  def apply(path: String = deviceGraphPath) = {
    path match {
      case p if p == deviceGraphPath => singleton
      case _ => new Graph(path)
    }
  }

}


class Graph(path: String) {
  def printNode(node: Node): Unit = {
    val props = for {
      prop <- node.getAllProperties.toList
      (k, v) = prop
    } yield (k,v)
    println(s"Node ${node.getId} :: " + props.mkString("::"))
  }

  def printString(): Unit = {
    val allNodes = graphDb.getAllNodes.iterator().toList
    allNodes.foreach(node => {
      printNode(node)
    })
  }

  val graphFile = new File(path)
//  if(graphFile.exists())

//  val params: java.util.Map[String, String] = new java.util.HashMap[String, String]
//  params.put("allow_store_upgrade", "true")
//  params.put("neostore.nodestore.db.mapped_memory", "100M")
//  params.put("neostore.relationshipstore.db.mapped_memory", "700M")
//  params.put("neostore.propertystore.db.mapped_memory", "1400M")
//  params.put("neostore.propertystore.db.strings.mapped_memory", "1500M")
//  params.put("neostore.propertystore.db.arrays.mapped_memory", "1M")

  val graphDb = open()

//  def createOrOpen() = {
//    new GraphDatabaseFactory().newEmbeddedDatabase()
//  }

  def open() = {
//    val graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(graphFile)
    val graphDb = new GraphDatabaseFactory()
      .newEmbeddedDatabase( graphFile )
//      .setConfig( GraphDatabaseSettings.pagecache_memory, "512M" )
//      .setConfig( GraphDatabaseSettings.string_block_size, "60" )
//      .setConfig( GraphDatabaseSettings.array_block_size, "300" )
//      .newGraphDatabase()

    graphDb
  }

  val loadLabel = new Label() {
    override def name(): String = "Load"
  }

  //todo: Could use typeclass here
  def persist(load: LoadNode) = {
    //How do we best consider uniqueness here - creating a new node vs updating?
    //Only one load of a given local id is allowed per project. Does that help?
    //Ignore the question for now...
    val newNode = nodeFromLoad(load)
  }

  def nodeFromLoad(load: LoadNode) = {
    val newNode = graphDb.createNode(loadLabel)
    newNode.setProperty("id", load.id)
    newNode.setProperty("localId", load.localId)
//    newNode.setProperty("", load.)
  }

}
