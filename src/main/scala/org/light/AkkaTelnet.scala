package org.light

import java.io.{PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket

import akka.actor.{Props, ActorSystem, Actor}

/**
 * Created by runger on 9/27/15.
 */


class AkkaTelnet(ip: String, user: String, pwd: String) {

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
      case str: String => {
        println(str)
      }
      case _       => println("huh?")
    }
  }

  val system = ActorSystem("LutronSystem")
  val lutronActor = system.actorOf(Props(new LutronActor))

  val readT = new Thread() {
    override def run(): Unit = {
      var ch = i.read().toChar
      while (ch != ':'){
//        print(ch)
        ch = i.read().toChar
      }
      lutronActor ! "login"

//      println("Login prompt received")
//      o.println(user + '\r')

      ch = i.read().toChar
      while (ch != ':'){
//        print(ch)
        ch = i.read().toChar
      }
      lutronActor ! "pwd"

//      println("Pwd prompt received")
//      o.println(pwd + '\r')

      var line = i.readLine()
      while (true){
        println(line)
        line = i.readLine()
        lutronActor ! line
      }
    }
  }

  readT.start()
}

object A extends App {
  val at = new AkkaTelnet("192.168.1.147", "lutron", "integration")

}