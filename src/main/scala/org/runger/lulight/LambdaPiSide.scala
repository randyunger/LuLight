package org.runger.lulight

import org.runger.lulight.lambda.LambdaHandler
import play.api.libs.json.{JsValue, Json}
import org.runger.lulight.lambda.model.HomeSkillFormats._

/**
  * Created by randy on 9/26/16.
  */



object LambdaPiSide {

}

class LambdaPiSide extends Logging {

  val mqttClient = new Mqtt(MqttAws.host, "RPIClient-" + "10228-" + System.currentTimeMillis().toString)

  val lambdaActioner = new LambdaDeviceActions()

  def publishRetainedDeviceList(): Unit = {
    //          val loads = LuConfig().storedConfig.search("60")  //office cans
    val loads = LuConfig().storedConfig.loads

    val skillDevices = loads.map(ll => {
      LambdaHandler.buildHomeSkillDevice(ll.id.toString, s"${ll.areaName} ${ll.outputName}", s"The ${ll.outputName} located in the ${ll.areaName}")
    }).toList

    val devicesMsgJV = Json.toJson(skillDevices)
    val devicesMsg = Json.stringify(devicesMsgJV)

    info(s"sending device list response: $devicesMsg")
    //Store a persistent message for anyone who connects
    mqttClient.publishAndRetain(LambdaHandler.topicListDevicesResponses, devicesMsg)
  }


  def initPiSideHandler(): Unit = {

    //Subscribe to device list requests and Action Requests
    val subscriptions = List(
      LambdaHandler.topicListDevicesRequests
      ,LambdaHandler.topicDeviceActions
    )

    mqttClient.subscribeMulti(subscriptions, (topic, msg) => {

      info(s"Aws Mqtt received. Topic: $topic Message: $msg")

      topic match {

        case LambdaHandler.topicListDevicesRequests => {
          publishRetainedDeviceList()
        }

        case LambdaHandler.topicDeviceActions => {
          info(s"Got a device request")
          val msgJv = Json.parse(msg)
          lambdaActioner.doAction(msgJv)
        }
      }
    })
    logger.info("Connected to AWS MQTT")
  }
}


class LambdaDeviceActions() extends Logging {

  def doAction(msg: JsValue) = {
    val action = (msg \ "header" \ "name").get.as[String]
    action match {
      case "TurnOffRequest" => {
        val id = (msg \ "payload" \ "appliance" \ "applianceId").get.as[String]
        val level = 0
        val loads = LuConfig().storedConfig.search(id)
        loads.foreach(load => {
          info(s"setting load $load to $level")
          CommandExecutor().execute(load.set(level))
        })
      }

      case "TurnOnRequest" => {
        val id = (msg \ "payload" \ "appliance" \ "applianceId").get.as[String]
        val level = 100
        val loads = LuConfig().storedConfig.search(id)
        loads.foreach(load => {
          info(s"setting load $load to $level")
          CommandExecutor().execute(load.set(level))
        })
      }

      case "SetPercentageRequest" => {
        val id = (msg \ "payload" \ "appliance" \ "applianceId").get.as[String]
        val level = (msg \ "payload" \"percentageState" \ "value").get.as[Int]
        val loads = LuConfig().storedConfig.search(id)
        loads.foreach(load => {
          info(s"setting load $load to $level")
          CommandExecutor().execute(load.set(level))
        })
      }

    }
  }

}