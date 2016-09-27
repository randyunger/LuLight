package org.runger.lulight

/**
  *
  * Created by Unger on 2/21/16.
  *
  **/

import org.runger.lulight.lambda.LambdaHandler
import org.specs2.mutable.Specification

class MqttTest extends Specification with Logging {

  "Mqtt" should {
    "pub" in {
      Mqtt().publish("something", "A message!")
      ok
    }
  }

//  "Mqtt" should {
//    "pub sub" in {
//      val topic = "something"
//      var cb = 0
//      Mqtt().subscribe(topic, (msg => {
//        cb = cb + 1
//        info(s"msg received: $msg")
//      }))
//      Mqtt().publish(topic, "A message!")
//      Thread.sleep(10*1000)
//      cb should(be_>=(1))
//    }//.pendingUntilFixed("this is failing the lambda update") //todo
//  }

  "Retained message" should {
    "be available on subscribe" in {
      val mqttClient = new Mqtt(MqttAws.host, "TestClient-" + "10228-" + System.currentTimeMillis().toString)
      var response = false

      mqttClient.subscribe(LambdaHandler.topicListDevicesResponses, (topic, msg) => {
        response = true
        println(s"got message: $msg")
      })

      while (response == false) Thread.sleep(100)

      ok
    }
  }

}
