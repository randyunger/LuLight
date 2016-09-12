package org.runger.lulight

/**
  * Created by randy on 9/11/16.
  */

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class LambdaHandler extends RequestHandler[Map[String, Object], String]{

  override def handleRequest(input: Map[String, Object], context: Context): String = {
    val logger = context.getLogger
    logger.log("Input received:")
    val s = input.toString()
    logger.log(s)
    "output!"
  }
}
