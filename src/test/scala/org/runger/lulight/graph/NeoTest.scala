//package org.runger.lulight.graph
//
///**
//  * Created by randy on 10/2/16.
//  */
//
//import org.anormcypher._
//import org.specs2.mutable.Specification
//import play.api.libs.ws.ning.NingWSClient
//
//class NeoTest extends Specification {
//
//  "cypher" should {
//
//    "connect JDBC" in {
//      ok
//    }
//
//
//    "anorm" in {
//      import scala.concurrent.ExecutionContext.Implicits.global
//      implicit val ws = NingWSClient()
//
//      // Setup the Rest Client
//      implicit val connection = Neo4jREST(host = "black-pearl", username = "neo4j", password = "neo4j")
//
//      // create some test nodes
//      Cypher("""create (anorm {name:"AnormCypher"}), (test {name:"Test"})""").execute()
//
//      // a simple query
//      val req = Cypher("start n=node(*) return n.name")
//
//      // get a stream of results back
//      val stream = req()
//
//      // get the results and put them into a list
//      stream.map(row => {row[String]("n.name")}).toList
//
//
//      ok
//    }
//  }
//
//}
