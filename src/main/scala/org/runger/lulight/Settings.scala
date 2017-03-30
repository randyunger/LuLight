package org.runger.lulight

import java.io.File

/**
  *
  * Created by Unger on 2/15/16.
  *
  **/

object Settings {
  val prodInstance = new FileSettings
  def apply() = prodInstance
}

trait Settings {
  def localOnly: Boolean
  def fakeTelnet: Boolean

  def moquetteHost: String
}

class HardSettings extends Settings {
  val localOnly = false
  val fakeTelnet = true

  val moquetteHost = "tcp://192.168.99.100:1883"
  val mosquittoHostAws = "tcp://52.44.173.143:1883" //"tcp://52.6.125.250:80"
}

class FileSettings extends HardSettings {
//  val classloader = Thread.currentThread().getContextClassLoader
//  val url = classloader.getResource("settings.txt")
//  val file = new File(url.toURI)

  override val fakeTelnet = false
  override val moquetteHost = "tcp://pi1:1883"
  override val mosquittoHostAws = "tcp://52.203.59.18:1883" //prev: "tcp://52.6.125.250:80"
}
