package org.light

/**
 * Created by runger on 9/27/15.
 */

import org.mortbay.jetty.Server
import org.mortbay.jetty.webapp.WebAppContext
import org.scalatra._
import scalate.ScalateSupport

class Endpoints extends ScalatraServlet with ScalateSupport {

  get("/") {
    <h1>Hello, world!</h1>
  }

}

object WebServerRunner extends App with LocalServer {
  private var continue = true

//  val sslParamPrefix = "--ssl="
//  val sslPortNumber = args.find(_ startsWith sslParamPrefix).map(str => str.drop(sslParamPrefix.length).tryToInt.getOrElse {
//    throw new IllegalArgumentException(s"--ssl port must be int-able; $str is not intable.")
//  })

  withIsolatedWebServer() { context=>
    /**
     * You can add handlers here so that you can do quick things to the runtime without restarting the server.
     */
    while(continue) {
//      if(sslPortNumber.isDefined) {
//        println("SSL note: If you don't want applications to warn you constantly about an untrusted certificate")
//        println("for your local server, you can add the following .crt path to your OS X Keychain as 'always trust'.")
//        println("Be sure to add it to the 'login' group there.")
//      }

      Console.readLine("Say 'cache' and hit enter to ditch permacacher and ehcache. Say 'exit' and hit enter to exit.\nWebServer> ") match {
        case "exit" | "end" | "quit" | ":q" =>
          println("Exiting...")
          continue = false
        case _ => {
          println("Doing nothing")
        }
      }
    }
  }
}

trait LocalServer {
  val webappcontext = new WebAppContext()
  var server: Option[Server] = None
  val defaultHttpPort = 8080
  lazy val keystorePath = getClass.getResource("localServerKeystore.jks").getPath
  lazy val certPath = getClass.getResource("localServer.crt").getPath

  /** If server is already started, just perform 'work' */
  def withWebServer(portNumber:Int = defaultHttpPort, sslPortNumber: Option[Int] = None, keepAlive: Boolean = false)(work: (WebAppContext) => Unit) {
    var thrownEx: Option[Throwable] = None
    try {
      startServer(portNumber, sslPortNumber)
    }
    catch {
      case t: Throwable => println(t.getMessage)
    }
    try {
      work(webappcontext)
    }
    catch {
      case t: Throwable if keepAlive => {
        println("Test failed. Keeping server alive anyway.")
        thrownEx = Some(t)
      }
    }

    if(keepAlive) Console.readLine("Press enter to shut down")
    thrownEx.foreach(ex => throw ex)
  }

  def withIsolatedWebServer(portNumber:Int = defaultHttpPort, sslPortNumber: Option[Int] = None)(work: (WebAppContext) => Unit) {
    startServer(portNumber, sslPortNumber)
    work(webappcontext)
    stopServer()
  }

  def startServer(portNumber: Int = defaultHttpPort, sslPortNumber: Option[Int] = None) = {
    server.getOrElse {
      val newServer = new Server(portNumber)

      //      for(sslPort <- sslPortNumber) {
      //        val sslContextFactory = new SslContextFactory()
      //        sslContextFactory.setKeyStorePath(keystorePath)
      //        sslContextFactory.setKeyStorePassword("aaaaaa")
      //        val sslConnector = new SslSocketConnector(sslContextFactory)
      //        sslConnector.setPort(sslPort)
      //        newServer.addConnector(sslConnector)
      //      }

      val testClassPath = this.getClass.getClassLoader.getResource(".").toString
      val contextPath = ""
      val webappcontext = new WebAppContext(testClassPath + "../../../src/main/webapp", contextPath)
      webappcontext.setServer(newServer)
      newServer.setHandler(webappcontext)
      newServer.start()
      server = Option(newServer)
      newServer
    }
  }

  def stopServer() {
    println("Stopping web server")
    server.map(_.stop())
  }
}

import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    println("")
//    context.mount(new CookiesExample, "/cookies-example")
//    context.mount(new FileUploadExample, "/upload")
//    context.mount(new FilterExample, "/")
//    context.mount(new HttpExample, "/*")
  }
}
