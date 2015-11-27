package org.lulight

import org.slf4j.LoggerFactory

/**
 * Created by Unger on 11/27/15.
 */
class Utils {

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
