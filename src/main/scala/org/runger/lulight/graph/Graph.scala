package org.runger.lulight.graph

import java.io.File

import org.neo4j.graphdb.factory.GraphDatabaseFactory

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

  val graphFile = new File(path)

  val params: java.util.Map[String, String] = new java.util.HashMap[String, String]
  params.put("allow_store_upgrade", "true")
  params.put("neostore.nodestore.db.mapped_memory", "100M")
  params.put("neostore.relationshipstore.db.mapped_memory", "700M")
  params.put("neostore.propertystore.db.mapped_memory", "1400M")
  params.put("neostore.propertystore.db.strings.mapped_memory", "1500M")
  params.put("neostore.propertystore.db.arrays.mapped_memory", "1M")

  val graphDb = create()

  def create() = {
    val graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(graphFile)
    graphDb
  }

}
