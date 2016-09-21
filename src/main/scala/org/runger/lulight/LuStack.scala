package org.runger.lulight

import javax.servlet.{ServletContextEvent, ServletContextListener}

import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate.{Binding, TemplateEngine}
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import javax.servlet.http.HttpServletRequest

import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory
import org.slf4j.helpers.SubstituteLoggerFactory

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

class Listener extends ServletContextListener with Logging {
  override def contextDestroyed(sce: ServletContextEvent): Unit = {}

  override def contextInitialized(sce: ServletContextEvent): Unit = {

//    val lf = LoggerFactory.getILoggerFactory
////    val nn = lf.asInstanceOf[SubstituteLoggerFactory]
//    val lc = lf.asInstanceOf[LoggerContext]
////    val n = nn.getLoggerNames
//    val ll = lc.getLoggerList

    //Load initial state
    info("Getting initial state")
    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
    fullState

    //Connect to Aws
    info("Connecting to AWS MQTT")
    MqttAws().subscribe("something", str => MqttAws.handleAwsEvent(str))
    info("Connected to AWS MQTT")
  }
}
