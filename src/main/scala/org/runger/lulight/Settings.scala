package org.runger.lulight

/**
  *
  * Created by Unger on 2/15/16.
  *
  **/

object Settings {
  val prodInstance = new Settings
  def apply() = prodInstance
}

class Settings {
  val localOnly = true
}
