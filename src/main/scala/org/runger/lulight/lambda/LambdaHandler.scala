package org.runger.lulight.lambda

/**
  * Created by randy on 9/11/16.
  */

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import org.runger.lulight.lambda.model._
import play.api.libs.json._
import HomeSkillFormats._
import ch.qos.logback.classic.LoggerContext
import org.runger.lulight.MqttAws._
import org.runger.lulight.{Mqtt, MqttAws}
import org.slf4j.LoggerFactory

object LambdaHandler {
  val charset = "UTF-8"
  val topicListDevicesRequests = s"/ha/lights/10228/ListDevicesRequests"
  val topicListDevicesResponses = s"/ha/lights/10228/ListDevicesResponses"
  val topicDeviceActions = s"/ha/lights/10228/DeviceActionRequests"
  var isLambda = false

  val defaultActions = List("setPercentage", "incrementPercentage", "decrementPercentage", "turnOff", "turnOn")
  val noDetails = AdditionalApplianceDetails(None, None, None, None)
  def buildHomeSkillDevice(applianceId: String, name: String, description: String) = {
    Appliance(
      defaultActions
      ,noDetails
      ,applianceId
      ,description
      ,name
      ,true //isReachable
      ,"CDMT"
      ,"LuLight1.0"
      ,"1.0"
    )
  }


}

class LambdaHandler extends RequestStreamHandler{

  def isFakeContext(context: Context): Boolean = {
    context.getAwsRequestId == "FakeRequestId"
  }


  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
//    val lc = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
//    lc.stop()
    LambdaHandler.isLambda = true

    val logger = context.getLogger

    val cmdJv = Json.parse(input)

    val loggableStr = Json.asciiStringify(cmdJv)
    logger.log(s"Input received: $loggableStr")


    val nameRes = cmdJv \ "header" \ "name"
    logger.log(s"name: $nameRes")

//    val daReq = p.asOpt[DiscoverAppliancesRequest]
    val header = (cmdJv \ "header").as[ReqHeader]

    val out = nameRes match {
      case JsDefined(JsString("DiscoverAppliancesRequest")) if isFakeContext(context) => discoverAppliancesFake(header) //todo: Remove
      case JsDefined(JsString("DiscoverAppliancesRequest")) => discoverAppliances(header, context)
      case JsDefined(JsString("TurnOnRequest")) => {"no response 2"} //todo
      case JsDefined(JsString("TurnOffRequest")) => turnOff(cmdJv, context)
      case JsUndefined() => {"error 1"} //todo
      case _ => {
        logger.log(s"Could not match request $nameRes")
        s"Sorry, I don't know how to handle that request, ${nameRes}"
      }
    }

    logger.log(s"output will be: $out")
    output.write(out.getBytes(LambdaHandler.charset))
    output.flush()
  }

  val lightActions = List("setPercentage", "incrementPercentage", "decrementPercentage", "turnOff", "turnOn")
  val fakeDetails = AdditionalApplianceDetails(None, None, None, None)

  def reqHeaderToRespHeader(dAReqHeader: ReqHeader): ResponseHeader = {
    val msgId = "lumsg" + dAReqHeader.messageId.hashCode
    ResponseHeader(msgId, "DiscoverAppliancesResponse", dAReqHeader.namespace, dAReqHeader.payloadVersion)
  }

  def turnOff(cmdJv: JsValue, context: Context): String = {
    val logger = context.getLogger
    logger.log("received turn off command")

    val mqttAws = new Mqtt(MqttAws.host, "LambdaClient" + this.hashCode.toString)
    var response = Option.empty[String]

//    val reqJv = Json.toJson(reqHeader)
    val reqJs = Json.stringify(cmdJv)

    mqttAws.publish(LambdaHandler.topicDeviceActions, reqJs)

    val hdr = ResponseHeader(System.currentTimeMillis().toString, "TurnOffConfirmation", "Alexa.ConnectedHome.Control", "2")
    val hdrJv = Json.toJson(hdr)

    val respJv = JsObject(Map("header" -> hdrJv, "payload" -> emptyObject))
    Json.stringify(respJv)
  }

  def discoverAppliances(dAReqHeader: ReqHeader, context: Context): String = {

    val mqttAws = new Mqtt(MqttAws.host, "LambdaClient" + this.hashCode.toString)
    var response = Option.empty[String]
    val logger = context.getLogger

    mqttAws.subscribe(LambdaHandler.topicListDevicesResponses, (topic, msg) => {
      logger.log("Received list devices response")
      response = Option(msg)
    })

    mqttAws.publish(LambdaHandler.topicListDevicesRequests, "pls")

    val startTime = System.currentTimeMillis()

    //15 second timeout in AWS Lambda
    def timeIsUp() = System.currentTimeMillis() - startTime > 15 * 1000

    while(response.isEmpty && !timeIsUp) {
      Thread.sleep(1000)
      logger.log("no response yet. Sleeping.")
    }

    logger.log(s"response received: $response")

    //todo: Need to block here until the RPi publishes the device list
    response match {
      case Some(respJs) => assembleAppliancesIntoResponse(respJs, dAReqHeader, context) match {
        case None => "could build response"
        case Some(respObj) => {
          val respJv = Json.toJson(respObj)
          val resp = Json.stringify(respJv)
          resp
        }
      }
      case _ =>  "No devices"
    }
  }

  def assembleAppliancesIntoResponse(appliancesJs: String, dAReqHeader: ReqHeader, context: Context): Option[DiscoverAppliancesResponse] = {
    val logger = context.getLogger

    val appJv = Json.parse(appliancesJs)
    val appList = appJv.asOpt[List[Appliance]]

    appList match {
      case Some(appliances) => {
        val header = reqHeaderToRespHeader(dAReqHeader)
        val respObj = DiscoverAppliancesResponse(header,
          DARespPayload(appliances)
        )
        Option(respObj)
      }
      case None => {
        logger.log(s"Couldnt parse response: $appliancesJs")
        None
      }
    }

  }

  def discoverAppliancesFake(dAReqHeader: ReqHeader): String = {
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
