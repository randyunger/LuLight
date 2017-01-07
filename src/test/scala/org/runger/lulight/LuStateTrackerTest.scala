package org.runger.lulight

import org.specs2.mutable.Specification

/**
  * Created by randy on 10/11/16.
  */
class LuStateTrackerTest extends Specification {

  "LuStateTracker" should {
    "go" in {
      LuConfig().storedConfig.withState
      ok
    }
  }

}
