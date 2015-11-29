package org.runger.lulight

import org.slf4j.LoggerFactory

/**
 * Created by Unger on 11/27/15.
 */

object Utils {

  implicit class StrUtils(val str: String) extends AnyVal {
    def tryToInt = {
      try {
        Option(str.toInt)
      } catch {
        case e: Exception => None
      }
    }

    def tryToFloat = {
      try {
        Option(str.toFloat)
      } catch {
        case e: Exception => None
      }
    }

  }

}



trait Logging {

  val logger = LoggerFactory.getLogger(this.getClass)

  def info(msg: String) = {
    logger.info(msg)
  }

  def warn(msg: String) = {
    logger.warn(msg)
  }

  def error(msg: String) = {
    logger.error(msg)
  }

}
