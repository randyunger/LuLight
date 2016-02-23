package org.runger.lulight

import org.eclipse.paho.client.mqttv3._
import play.api.libs.json.Json

/**
  *
  * Created by Unger on 2/21/16.
  *
  **/

//todo: Example code
//https://github.com/eclipse/paho.mqtt.java/blob/master/org.eclipse.paho.sample.mqttv3app/src/main/java/org/eclipse/paho/sample/mqttv3app/Sample.java

object Mqtt {
  val host = "tcp://localhost:1883"
  val clientId = "LuLight"  //Would have to make this unique for multiple servers.
  val prodInstance = new Mqtt(Mqtt.host, Mqtt.clientId + this.hashCode.toString)
  def apply() = prodInstance
}

class Mqtt(host: String, clientId: String) extends Logging {
  val client = new MqttClient(host, clientId)

  val connOps = new MqttConnectOptions()
  connOps.setCleanSession(false)

  client.connect(connOps)

  def publish(loadId: Int, loadState: LoadState): Unit = {
//    Mqtt().publish(s"/ha/lights/10228/${load.id}", st.level.toString)
    val topic = s"/ha/lights/10228/$loadId"
    val jv = Json.toJson(loadState)
    val msg = Json.asciiStringify(jv)
    publish(topic, msg)
  }

  def publish(topic: String, msg: String): Unit = {
    try {
      val bytes = msg.getBytes
      val mqMsg = new MqttMessage(bytes)
      mqMsg.setQos(2)
      client.publish(topic, mqMsg)
    } catch {
      case ex: Exception => {
        warn(s"mqtt exception", ex)
      }
    }
  }

  def subscribe(topic: String, f: String => Unit) = {
    val cb = new MqttCallback {
      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        info("Executing Mqtt callback")
        val payload = new String(message.getPayload)
        val res = f(payload)
        info(s"payload: $payload")
      }

      override def connectionLost(cause: Throwable): Unit = {}
    }

    client.setCallback(cb)

    client.subscribe(topic)
  }

}
