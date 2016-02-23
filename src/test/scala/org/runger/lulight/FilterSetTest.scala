package org.runger.lulight

/**
  *
  * Created by Unger on 2/19/16.
  *
  **/


import org.specs2.mutable.Specification

class FilterSetTest extends Specification {

  "FilterSetTest" should {
    "filter" in {
      val fs = FilterSet(floors = Set(Floor.Downstairs))
      val startingLoads = LuConfig().storedConfig
      val res = fs.filter(startingLoads)
      assert(res.loads.size < startingLoads.loads.size)
      assert(res.loads.size > 0)
      ok
    }
  }

  "FilterSetTest with Two Filters" should {
    "filter with two filters" in {
      val fs = FilterSet(floors = Set(Floor.Downstairs), bulbTypes = Set(BulbType.LED))
      val startingLoads = LuConfig().storedConfig
      val res = fs.filter(startingLoads)
      println("With two filters: ")
      res.loads.foreach(println)
      assert(res.loads.size < startingLoads.loads.size)
      assert(res.loads.size > 0)
      ok
    }
  }

}
