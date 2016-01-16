package org.runger.lulight

import java.io.{PrintWriter, InputStreamReader, BufferedReader}
import java.net.{InetSocketAddress, Socket}
import akka.actor.{Props, ActorSystem, Actor}
import scala.collection.mutable
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

object TelnetDebug extends App {
  val ip = "192.168.1.2"
  val user="lutron"
  val pwd = "integration"
  val telnetPort = 23
  val sock = new Socket(ip, telnetPort)
  val i = new LuBuff(new InputStreamReader(sock.getInputStream, "US-ASCII"))
  val o = new PrintWriter(sock.getOutputStream, true)
  var char = 102.toChar
  while (char!=':') {
    char = i.read().toChar
    print(char.toInt + "\t" + char)
  }
  o.println(user + '\r')
  while (char!=':') {
    char = i.read().toChar
    print(char.toInt + "\t" + char)
  }
  o.println(pwd + '\r')
}

class CommPackage(addr: String) extends Logging {

  val telnetPort = 23
  info(s"Connecting to $addr port $telnetPort")

  val sock = new Socket(addr, telnetPort)
  info(s"Connected to $addr port $telnetPort")
  val socketIsIntact = true
  val i = new LuBuff(new InputStreamReader(sock.getInputStream, "US-ASCII"))
  val o = new PrintWriter(sock.getOutputStream, true)

  def send(message: String) = {
    o.println(message + '\r')
  }

  def reconnect() = {
    val prev = sock.getLocalSocketAddress
    sock.connect(prev)
  }

  def close() = {
    sock.close()
  }

}

class TelnetClient(ip: String, user: String, pwd: String) extends Logging {

  var comm = new CommPackage(ip)

  class LutronActor extends Actor {
    def receive = {
      case "login" => {
        info("Login prompt received")
        comm.send(user)
      }
      case "pwd" => {
        info("Pwd prompt received")
        comm.send(pwd)
      }
      case cmd: String => {
        if(!hasLoggedIn){
          warn("Trying to send cmd before log-in")
          system.scheduler.scheduleOnce(1000 milliseconds, lutronActor, cmd)
        } else {
          info(s"Sending telnet cmd: $cmd")
          comm.send(cmd)
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
      val lineBuff = mutable.Buffer.empty[String]

      var ch = comm.i.read().toChar
      chBuff.append(ch)
//      info(s"rcv: $ch")

      //Read Login prompt
      while (ch != ':'){
        ch = comm.i.read().toChar
        chBuff.append(ch)
      }
      info(s"rcv: $chBuff")
      lutronActor ! "login"

      //Read password prompt
      ch = comm.i.read().toChar
      while (ch != ':'){
        ch = comm.i.read().toChar
        chBuff.append(ch)
      }
      info(s"rcv: $chBuff")
      lutronActor ! "pwd"

      //Send subsequent output to Actor
//      ch = i.read().toChar
      //      chBuff.append(ch)
      var line = comm.i.readLine()
      info(line)
      lineBuff.append(line)
      while (true) {
        try {
          //Check for post-login string
          //        if(!hasLoggedIn && chBuff.indexOf("GNET") > -1) {
          if (!hasLoggedIn && line.contains("GNET")) {
            info("We've received log in confirmation!")
            hasLoggedIn = true
          }
          line = comm.i.readLine()
          info(s"rcv line: $line")
          lineBuff.append(line)
          LuStateTracker().update(line)
          //        val str = chBuff.toString()
          //        if(chBuff.length > 2000) {
          //          val tmp = chBuff.takeRight(1000)
          //          chBuff.clear()
          //          chBuff.append(tmp)
          //        }
          //        info(s"rcv: $chBuff")
        } catch {
          case e: Exception => {
            warn("Warning: Socket was disconnected!")
            warn(e.getStackTrace.mkString("\n"))
            e.printStackTrace()
            comm.close()
            comm = new CommPackage(ip)
          }
        }
        finally {

        }
      }
    }
  }
  readT.start()

  def execute(cmd: String) = {
    lutronActor ! cmd
  }
}
