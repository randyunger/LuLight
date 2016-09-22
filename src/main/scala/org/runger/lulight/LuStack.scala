package org.runger.lulight

import javax.servlet.{ServletContextEvent, ServletContextListener}

import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate.{Binding, TemplateEngine}
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import javax.servlet.http.HttpServletRequest

import ch.qos.logback.classic.LoggerContext
import org.runger.lulight.lambda.LambdaHandler
import org.slf4j.LoggerFactory
import org.slf4j.helpers.SubstituteLoggerFactory
import play.api.libs.json.Json
import org.runger.lulight.lambda.model.HomeSkillFormats._

trait LuStack extends ScalatraServlet with ScalateSupport {

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }

}

class LuServletContextListener extends ServletContextListener with Logging {
  override def contextDestroyed(sce: ServletContextEvent): Unit = {}

  override def contextInitialized(sce: ServletContextEvent): Unit = {

    //Load initial state
    logger.info("Getting initial state")
    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
    fullState

    //Connect to Aws
    logger.info("Connecting to AWS MQTT")

    val mqttAws = new Mqtt(MqttAws.host, "RPIClient" + this.hashCode.toString)

    //Subscribe to device list requests and Action Requests
    val subscriptions = List(
      LambdaHandler.topicListDevicesRequests
      ,LambdaHandler.topicDeviceAction
    )

    mqttAws.subscribeMulti(subscriptions, (topic, msg) => {

      topic match {

        case LambdaHandler.topicListDevicesRequests => {
          info(s"Aws Mqtt received. Topic: topicListDevicesRequests Message: $msg")

          val loads = LuConfig().storedConfig.search("60")  //office cans

          val skillDevices = loads.map(ll => {
            LambdaHandler.buildHomeSkillDevice(ll.id.toString, ll.outputName, s"${ll.areaName} - ${ll.outputName}")
          }).toList

          val devicesMsgJV = Json.toJson(skillDevices)
          val devicesMsg = Json.stringify(devicesMsgJV)

          info(s"sending device list response: $devicesMsg")
          mqttAws.publish(LambdaHandler.topicListDevicesResponses, devicesMsg)
        }

        case LambdaHandler.topicDeviceAction => {
          info(s"Aws Mqtt received. Topic: topicDeviceAction Message: $msg")
        }
      }

    })

    logger.info("Connected to AWS MQTT")
  }
}
