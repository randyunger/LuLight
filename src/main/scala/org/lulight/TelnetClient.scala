package org.lulight

import java.io.{PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket

import akka.actor.{Props, ActorSystem, Actor}

/**
 * Created by Unger on 9/30/15.
 */

object TelnetClient {

  val instance = new TelnetClient("192.168.1.147", "lutron", "integration")

  def apply() = {
    instance
  }


}

class TelnetClient(ip: String, user: String, pwd: String) {

  val sock = new Socket(ip, 23)
  val i = new BufferedReader(new InputStreamReader(sock.getInputStream))
  val o = new PrintWriter(sock.getOutputStream, true)

  class LutronActor extends Actor {
    def receive = {
      case "login" => {
        println("Login prompt received")
        o.println(user + '\r')
      }
      case "pwd" => {
        println("Pwd prompt received")
        o.println(pwd + '\r')
      }
      case cmd: String => {
        println(s"Sending telnet cmd: $cmd")
        o.println(cmd + '\r')
      }
      case _ => println("Unknown cmd to Actor")
    }
  }

  val system = ActorSystem("LutronSystem")
  val lutronActor = system.actorOf(Props(new LutronActor))

  val readT = new Thread() {
    override def run(): Unit = {
      var ch = i.read().toChar

      //Read Login prompt
      while (ch != ':'){
        ch = i.read().toChar
      }
      lutronActor ! "login"

      //Read password prompt
      ch = i.read().toChar
      while (ch != ':'){
        ch = i.read().toChar
      }
      lutronActor ! "pwd"

      //Send subsequent output to Actor
      var line = i.readLine()
      while (true){
        println(line)
        line = i.readLine()
      }
    }
  }

  readT.start()


  def execute(cmd: String) = {
    lutronActor ! cmd
  }
}
