package org.runger.lulight

/**
 * Created by Unger on 9/30/15.
 */

object LuConsole extends App {
  println("Connecting telnet...")
  val at = new TelnetClient("192.168.1.147", "lutron", "integration")
  println("Downloading schema...")
  val lu = LuConfig.parseXml
  println("Ready for input...")

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