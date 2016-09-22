package org.runger.lulight

import com.amazonaws.services.lambda.runtime.LambdaLogger
import org.runger.lulight.lambda.LambdaHandler
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

//object Logging {
//  def apply(): Logging = {
//    if(LambdaHandler.isLambdaEnv) {
//
//    }
//  }
//}

trait Logging {
  def info(msg: String)
  def warn(msg: String)
}

class LamdbaLoggerWrapper(ll: LambdaLogger) extends Logging {
  def info(msg: String) = try {
    ll.log(msg)
  } catch {
    case ex: Exception => println("Logging exception in info")
  }

  def warn(msg: String) = try {
    ll.log(msg)
  } catch {
    case ex: Exception => println("Logging exception in warn")
  }
}

trait LoggingImpl extends Logging {

  val logger = LoggerFactory.getLogger(this.getClass)

  def info(msg: String) = try {
    logger.info(msg)
  } catch {
    case ex: Exception => println("Logging exception in LoggingImpl info")
  }

  def warn(msg: String) = try {
    logger.warn(msg)
  } catch {
    case ex: Exception => println("Logging exception in LoggingImpl warn")
  }

  def warn(msg: String, t: Throwable) = try {
    logger.warn(msg, t)
  } catch {
    case ex: Exception => println("Logging exception in LoggingImpl t")
  }

  def error(msg: String) = try {
    logger.error(msg)
  } catch {
    case ex: Exception => println("Logging exception in LoggingImpl error")
  }

}
