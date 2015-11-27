package org.lulight

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

/**
 * Created by Unger on 10/4/15.
 */
object WebServerRunner extends App with LocalServer with Logging {

//  new Thread {
//    info("Starting telnet")
//    val tc = TelnetClient()
//  }.start()
//
//  new Thread {
//    info("Reading config")
//    val lu = LuConfig()
//  }.start()

  private var continue = true

  withIsolatedWebServer() { context=>
    /**
     * You can add handlers here so that you can do quick things to the runtime without restarting the server.
     */
    while(continue) {

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

trait LocalServer extends Logging {
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
      case t: Throwable => info(t.getMessage)
    }
    try {
      work(webappcontext)
    }
    catch {
      case t: Throwable if keepAlive => {
        info("Test failed. Keeping server alive anyway.")
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
      val webappcontext = new WebAppContext(testClassPath + "../../../src/main/webapp", "")
      webappcontext.setServer(newServer)
      newServer.setHandler(webappcontext)
      newServer.start()
      server = Option(newServer)
      newServer
    }
  }

  def stopServer() {
    info("Stopping web server")
    server.map(_.stop())
  }
}
