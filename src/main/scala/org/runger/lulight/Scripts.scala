package org.runger.lulight

/**
  * Created by randy on 4/25/18.
  */



object Scripts {


}


object SubAllLocal extends App {

  println("Subscribing to all aka #")

  Mqtt().subscribe("#", (topic, payload) => println(s"Topic: $topic Payload: $payload"))

  println("Subscribed")

}

object SendTestMessageLocal extends App {

  Mqtt().publish("Test", "sent a message")

  println("Published message")

  System.exit(1)

}


object SubAllAws extends App {

  println("Subscribing to AWS all aka #")

  val mqttClient = new Mqtt(MqttAws.host, "RPIClient-" + "10228-" + System.currentTimeMillis().toString)
  mqttClient.subscribe("#", (topic, payload) => println(s"Topic: $topic Payload: $payload"))

  println("Subscribed")

}

object SendTestMessageRemote extends App {

  val mqttClient = new Mqtt(MqttAws.host, "RPIClient-" + "10228-2-" + System.currentTimeMillis().toString)
  mqttClient.publish("Test", "sent a message remote")

  println("Published message remote")

  System.exit(1)

}