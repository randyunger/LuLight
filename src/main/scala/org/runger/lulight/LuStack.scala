package org.runger.lulight

import javax.servlet.{ServletContextEvent, ServletContextListener}

import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate.{Binding, TemplateEngine}
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import javax.servlet.http.HttpServletRequest

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.runger.lulight.lambda.LambdaHandler

import collection.mutable

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

class Listener(implicit val bindingModule: BindingModule) extends ServletContextListener with Injectable {

  val logger = new LoggingImpl {}

  override def contextDestroyed(sce: ServletContextEvent): Unit = {}

  override def contextInitialized(sce: ServletContextEvent): Unit = {

    //Load initial state
    logger.info("Getting initial state")
    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
    fullState

    //Connect to Aws
    logger.info("Connecting to AWS MQTT")

    //Subscribe to device list requests
    val mqttAws = new Mqtt(MqttAws.host, MqttAws.clientId + this.hashCode.toString)
    mqttAws.subscribe(LambdaHandler.topicListDevices, str => MqttAws.handleAwsEventLocally(LambdaHandler.topicListDevices, str))

    //Subscribe to action requests
    mqttAws.subscribe(LambdaHandler.topicDeviceAction, str => MqttAws.handleAwsEventLocally(LambdaHandler.topicDeviceAction, str))
    logger.info("Connected to AWS MQTT")
  }
}
