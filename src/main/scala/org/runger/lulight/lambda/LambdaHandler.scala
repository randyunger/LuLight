package org.runger.lulight.lambda

/**
  * Created by randy on 9/11/16.
  */

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{Context, LambdaLogger, RequestStreamHandler}
import org.runger.lulight.lambda.model._
import play.api.libs.json.{JsDefined, JsString, JsUndefined, Json}
import HomeSkillFormats._
import ch.qos.logback.classic.LoggerContext
import org.runger.lulight._
import org.slf4j.LoggerFactory

object LambdaHandler {
  val topicListDevices = s"/ha/lights/10228/ListDevicesRequests"
  val topicDeviceAction = s"/ha/lights/10228/DeviceActionRequests"
  var isLambdaEnv = false

//  private var logger: Logging = new LoggingImpl {}
  def getLogger(context: Context): Logging = {
//    val lc = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
//    lc.stop()
    new LamdbaLoggerWrapper(context.getLogger)
  }
}

class LambdaHandler extends RequestStreamHandler {

  def isFakeContext(context: Context): Boolean = {
    context.getAwsRequestId == "FakeRequestId"
  }

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    LambdaHandler.isLambdaEnv = true

    val logger = LambdaHandler.getLogger(context)

    val p = Json.parse(input)
    val ostr = Json.asciiStringify(p)

    logger.info(s"Input received: $ostr")


    val nameRes = p \ "header" \ "name"
    logger.info(s"name: $nameRes")

//    val daReq = p.asOpt[DiscoverAppliancesRequest]
    val header = (p \ "header").as[DAReqHeader]

    val out = nameRes match {
      case JsDefined(JsString("DiscoverAppliancesRequest")) if isFakeContext(context) => discoverAppliancesFake(header) //todo: Remove
      case JsDefined(JsString("DiscoverAppliancesRequest")) => discoverAppliances(header, context)
      case JsDefined(JsString("otherTypeGoesHere")) => {"no response 2"} //todo
      case JsUndefined() => {"error 1"} //todo
    }

    logger.info(s"output will be: $out")
    output.write(out.getBytes("UTF-8"))
    output.flush()
  }

  val lightActions = List("setPercentage", "incrementPercentage", "decrementPercentage", "turnOff", "turnOn")
  val fakeDetails = AdditionalApplianceDetails(None, None, None, None)

  def reqHeaderToRespHeader(dAReqHeader: DAReqHeader): DARespHeader = {
    DARespHeader("myMessageID", "DiscoverAppliancesResponse", dAReqHeader.namespace, dAReqHeader.payloadVersion)
  }

  def discoverAppliances(dAReqHeader: DAReqHeader, context: Context): String = {
    val logger = LambdaHandler.getLogger(context)
    val mqttAws = new Mqtt(MqttAws.host, MqttAws.clientId + this.hashCode.toString)
    mqttAws.publish(LambdaHandler.topicListDevices, "pls")
    //todo: Need to block here until the RPi publishes the device list
    "ok"
  }

  def discoverAppliancesFake(dAReqHeader: DAReqHeader): String = {
    val daResp = DiscoverAppliancesResponse(reqHeaderToRespHeader(dAReqHeader),
      DARespPayload(List(
        Appliance(
          lightActions
          ,fakeDetails
          ,"12345ID"
          ,"A fake appliance!"
          ,"Fakey"
          ,true
          ,"CDMT"
          ,"LuLight1.0"
          ,"1.0"
        )
      ))
    )

    val jv = Json.toJson(daResp)
    val jOut = Json.stringify(jv)
    jOut
  }

}
