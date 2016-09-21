package org.runger.lulight

import org.eclipse.paho.client.mqttv3._
import org.runger.lulight.lambda.LambdaHandler
import play.api.libs.json.Json

/**
  *
  * Created by Unger on 2/21/16.
  *
  **/

//todo: Example code
//https://github.com/eclipse/paho.mqtt.java/blob/master/org.eclipse.paho.sample.mqttv3app/src/main/java/org/eclipse/paho/sample/mqttv3app/Sample.java

object Mqtt {
  val host = Settings().moquetteHost //"tcp://192.168.99.100:1883"
  val clientId = "LuLight"  //Would have to make this unique for multiple servers.
  val prodInstance = new Mqtt(Mqtt.host, Mqtt.clientId + this.hashCode.toString, new LoggingImpl {})
  def apply() = prodInstance
}

object MqttAws {
//  val logger = new LamdbaLoggerWrapper() {}

  val host = Settings().moquetteHostAws //"tcp://52.6.125.250:80"
  val clientId = "LuLightToAws"  //Would have to make this unique for multiple servers.
  val prodInstance = new Mqtt(host, clientId + this.hashCode.toString, logger)
  def apply() = prodInstance

  def handleAwsEventLocally(topic: String, str: String): Unit = {
    logger.info(s"Received mqtt from AWS topic $topic: $str")
  }

}

//Todo: Logger needs to be injected
class Mqtt(host: String, clientId: String, logger: Logging) {
  val client = new MqttClient(host, clientId)

  val connOps = new MqttConnectOptions()
  connOps.setCleanSession(false)

  def publish(loadId: Int, loadState: LoadState): Unit = {
//    Mqtt().publish(s"/ha/lights/10228/${load.id}", st.level.toString)
    val topic = s"/ha/lights/10228/$loadId"
    val jv = Json.toJson(loadState)
    val msg = Json.asciiStringify(jv)
    publish(topic, msg)
  }

  def publish(topic: String, msg: String): Unit = {
    try {
      if(!client.isConnected) client.connect()
      val bytes = msg.getBytes
      val mqMsg = new MqttMessage(bytes)
      mqMsg.setQos(2)
      client.publish(topic, mqMsg)
    } catch {
      case ex: Exception => {
        logger.warn(s"mqtt exception: " + ex.getMessage)
      }
    }
  }

  def subscribe(topic: String, f: String => Unit) = {
    if(!client.isConnected) client.connect()
    val cb = new MqttCallback {
      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}

      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        logger.info("Executing Mqtt callback")
        val payload = new String(message.getPayload)
        val res = f(payload)
        logger.info(s"topic: $topic payload: $payload")
      }

      override def connectionLost(cause: Throwable): Unit = {
        logger.warn("LuLight lost connection to MQTT broker.")

        //reconnect logic here
        if(!client.isConnected) {
          logger.info("retrying mqtt connection")
          Thread.sleep(10*1000)
          try {
            client.connect()
            //todo: does this not resubscribe? If we want to resubscribe, set clean session = false
          } catch {
            case t: Throwable => connectionLost(t)
          }
          logger.info("MQTT reconnected")
        }
      }
    }

    client.setCallback(cb)

    client.subscribe(topic)
  }

}

