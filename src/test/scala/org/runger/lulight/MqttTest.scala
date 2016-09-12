package org.runger.lulight

/**
  *
  * Created by Unger on 2/21/16.
  *
  **/

import org.specs2.mutable.Specification

class MqttTest extends Specification with Logging {

  "Mqtt" should {
    "pub" in {
      Mqtt().publish("something", "A message!")
      ok
    }
  }

  "Mqtt" should {
    "pub sub" in {
      val topic = "something"
      var cb = 0
      Mqtt().subscribe(topic, (msg => {
        cb = cb + 1
        info(s"msg received: $msg")
      }))
      Mqtt().publish(topic, "A message!")
      Thread.sleep(10*1000)
      cb should(be_>=(1))
    }
  }
}
