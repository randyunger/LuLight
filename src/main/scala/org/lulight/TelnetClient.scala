package org.lulight

import java.io.{PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket
import akka.actor.{Props, ActorSystem, Actor}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Unger on 9/30/15.
 */

object TelnetClient {

  def init() = {}

  val instance = new TelnetClient(LuConfig.repeaterIpAddress, "lutron", "integration")

  def apply() = {
    instance
  }


}

class TelnetClient(ip: String, user: String, pwd: String) extends Logging {

  val telnetPort = 23

  info(s"Connecting to $ip port $telnetPort")

  val sock = new Socket(ip, telnetPort)
  info(s"Connected to $ip port $telnetPort")

  val i = new BufferedReader(new InputStreamReader(sock.getInputStream))
  val o = new PrintWriter(sock.getOutputStream, true)

  class LutronActor extends Actor {
    def receive = {
      case "login" => {
        info("Login prompt received")
        o.println(user + '\r')
      }
      case "pwd" => {
        info("Pwd prompt received")
        o.println(pwd + '\r')
      }
      case cmd: String => {
        if(!hasLoggedIn){
          warn("Trying to send cmd before log-in")
          system.scheduler.scheduleOnce(1000 milliseconds, lutronActor, cmd)
        } else {
          info(s"Sending telnet cmd: $cmd")
          o.println(cmd + '\r')
        }
      }
      case _ => warn("Unknown cmd to Actor")
    }
  }

  val system = ActorSystem("LutronSystem")
  val lutronActor = system.actorOf(Props(new LutronActor))

  var hasLoggedIn = false

  val readT = new Thread() {
    override def run(): Unit = {

      val chBuff = new StringBuilder(200)

      var ch = i.read().toChar
      chBuff.append(ch)
//      info(s"rcv: $ch")

      //Read Login prompt
      while (ch != ':'){
        ch = i.read().toChar
        chBuff.append(ch)
      }
      info(s"rcv: $chBuff")
      lutronActor ! "login"

      //Read password prompt
      ch = i.read().toChar
      while (ch != ':'){
        ch = i.read().toChar
        chBuff.append(ch)
      }
      info(s"rcv: $chBuff")
      lutronActor ! "pwd"

      //Send subsequent output to Actor
      ch = i.read().toChar
      chBuff.append(ch)
      while (true){
        //Check for post-login string
        if(!hasLoggedIn && chBuff.indexOf("GNET") > -1) {
          hasLoggedIn = true
        }
        ch = i.read().toChar
        chBuff.append(ch)
        val str = chBuff.toString()
        if(chBuff.length > 2000) {
          val tmp = chBuff.takeRight(1000)
          chBuff.clear()
          chBuff.append(tmp)
        }
//        info(s"rcv: $chBuff")
      }
    }
  }

  readT.start()


  def execute(cmd: String) = {
    lutronActor ! cmd
  }
}
