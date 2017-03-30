package org.runger.lulight

import javax.servlet.{ServletContextEvent, ServletContextListener}

import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate.{Binding, TemplateEngine}
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import javax.servlet.http.HttpServletRequest

import ch.qos.logback.classic.LoggerContext
import org.runger.lulight.graph.Graph
import org.runger.lulight.lambda.LambdaHandler
import org.slf4j.LoggerFactory
import org.slf4j.helpers.SubstituteLoggerFactory
import play.api.libs.json.{JsValue, Json}
import org.runger.lulight.lambda.model.HomeSkillFormats._

import scala.concurrent.Future

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

    //Load object Graph
    logger.info("Loading graph")
//    Graph()

    //Load initial state
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      logger.info("Getting initial state")
      val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
      fullState
    }

    //Connect to Aws
    try {
      logger.info("Connecting to AWS MQTT")
      val handler = new LambdaPiSide
      handler.initPiSideHandler()
      handler.publishRetainedDeviceList()
    } catch {
      case ex: Exception => {
        logger.error("Error connecting to AWS!", ex)
      }
    }

  }
}

