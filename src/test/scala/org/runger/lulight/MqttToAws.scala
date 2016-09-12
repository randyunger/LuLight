package org.runger.lulight

/**
  * Created by randy on 9/11/16.
  */

import org.specs2.mutable.Specification

class MqttToAws extends Specification with Logging {

  "Mqtt to Aws" should {
    "pub" in {
      val client = new Mqtt(Settings().moquetteHostAws, "devClient1")
      client.publish("something", "A message!")
      ok
    }
  }
}