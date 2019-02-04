package org.runger.lulight

/**
  * Created by randy on 9/11/16.
  */

import org.specs2.mutable.Specification

class MqttToAws extends Specification with Logging {

  "Mqtt to Aws" should {
    "pub" in {
      val client = new Mqtt(Settings().mosquittoHostAws, "devClient1")
      client.publish("something", "A message!")
      ok
    }
  }

  "Mqtt to Aws mosquitto" should {
    "pub" in {

      //skipped

      println("Opening connection")
      val client = new Mqtt(Settings().mosquittoHostAws, "mosqClient")
      println("Connection opened")

      client.subscribe("something", (t, m) => println(s"t: $t m: $m "))

      println("Publishing msg")
      client.publish("something", "A message!")
      println("msg published")
      ok
    }
  }
}