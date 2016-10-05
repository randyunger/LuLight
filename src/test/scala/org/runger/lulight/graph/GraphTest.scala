package org.runger.lulight.graph

import org.specs2.mutable.Specification

/**
  * Created by randy on 10/5/16.
  */


class GraphTest extends Specification {
  "Graph" should {
    "create" in {
      Graph("/opt/neo4j/deviceGraphTest")

      ok
    }
  }
}
