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

  def fileFromUri(uri: String) = {
    val lastSlash = uri.lastIndexOf("/")+1
//    val toTake = uri.length - lastSlash
//    val max = math.max(lastSlash, 0)
    val file = uri.substring(lastSlash)
    file
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

  def warn(msg: String, t: Throwable) = {
    logger.warn(msg, t)
  }

  def error(msg: String) = {
    logger.error(msg)
  }

}
