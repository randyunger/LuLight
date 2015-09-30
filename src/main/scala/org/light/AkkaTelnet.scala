package org.light

import java.io.{PrintWriter, InputStreamReader, BufferedReader}
import java.net.Socket
import scala.collection.immutable.StringOps
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

object A extends App {
  val at = new AkkaTelnet("192.168.1.147", "lutron", "integration")
  val lu = LuConfig.parseXml

  var continue = true
  while(continue) {
    Console.readLine("Enter cmd: ") match {

      case search: String if search.startsWith("s ") =>
        val res = lu.search(search.drop(2))
        res.foreach(load => {
          println(load)
        })

      case "exit" | "end" | "quit" | ":q" =>
        println("Exiting...")
        continue = false

      case cmd: String => {
        println("Executing telnet cmd")
        at.execute(cmd)
      }

      case _ => {
        println("Doing nothing")
      }
    }
  }
}