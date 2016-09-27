package org.runger.lulight.lambda

/**
  * Created by randy on 9/11/16.
  */

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import org.runger.lulight.lambda.model._
import play.api.libs.json._
import HomeSkillFormats._
import org.runger.lulight.{Mqtt, MqttAws}
//import scala.concurrent.Future

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

  def getMqttClient() = {
    new Mqtt(MqttAws.host, "LambdaClient-10228-" + System.currentTimeMillis())
  }

  //Share one client across requests (??) instead of a new client each time.
  //Mosquitto broker seems to be acting weird with so many clients connected.
  //val mqttClient = getMqttClient()
//  def mqttClient = getMqttClient()

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    LambdaHandler.isLambda = true
    val logger = context.getLogger
    val mqttClient = getMqttClient()

    val cmdJv = Json.parse(input)

    val loggableStr = Json.asciiStringify(cmdJv)
    logger.log(s"Input received: $loggableStr")

    val nameRes = cmdJv \ "header" \ "name"
    logger.log(s"name: $nameRes")

    val header = (cmdJv \ "header").as[ReqHeader]

    val out = nameRes match {
      case JsDefined(JsString("DiscoverAppliancesRequest")) if isFakeContext(context) => discoverAppliancesFake(header) //todo: Remove
      case JsDefined(JsString("DiscoverAppliancesRequest")) => discoverAppliances(mqttClient, header, context)
      case JsDefined(JsString("TurnOnRequest")) => turnOn(mqttClient, cmdJv, context)
      case JsDefined(JsString("TurnOffRequest")) => turnOff(mqttClient, cmdJv, context)
      case JsDefined(JsString("SetPercentageRequest")) => setPercentage(mqttClient, cmdJv, context)
      case JsUndefined() => {"error 1"} //todo
      case _ => {
        logger.log(s"Could not match request $nameRes")
        s"Sorry, I don't know how to handle that request, $nameRes"
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

  def turnOff(mqttClient: Mqtt, cmdJv: JsValue, context: Context): String = {
    val logger = context.getLogger
    logger.log("received turn off command")

    var response = Option.empty[String]
    val reqJs = Json.stringify(cmdJv)

    mqttClient.publish(LambdaHandler.topicDeviceActions, reqJs)

    val hdr = ResponseHeader(System.currentTimeMillis().toString, "TurnOffConfirmation", "Alexa.ConnectedHome.Control", "2")
    val hdrJv = Json.toJson(hdr)

    val respJv = JsObject(Map("header" -> hdrJv, "payload" -> emptyObject))
    Json.stringify(respJv)
  }

  def turnOn(mqttClient: Mqtt, cmdJv: JsValue, context: Context): String = {
    val logger = context.getLogger
    logger.log("received turn on command")

    var response = Option.empty[String]
    val reqJs = Json.stringify(cmdJv)

    mqttClient.publish(LambdaHandler.topicDeviceActions, reqJs)

    val hdr = ResponseHeader(System.currentTimeMillis().toString, "TurnOnConfirmation", "Alexa.ConnectedHome.Control", "2")
    val hdrJv = Json.toJson(hdr)

    val respJv = JsObject(Map("header" -> hdrJv, "payload" -> emptyObject))
    Json.stringify(respJv)
  }

  def setPercentage(mqttClient: Mqtt, cmdJv: JsValue, context: Context): String = {
    val logger = context.getLogger
    logger.log("received set percentage command")

    var response = Option.empty[String]
    val reqJs = Json.stringify(cmdJv)

    mqttClient.publish(LambdaHandler.topicDeviceActions, reqJs)

    val hdr = ResponseHeader(System.currentTimeMillis().toString, "SetPercentageConfirmation", "Alexa.ConnectedHome.Control", "2")
    val hdrJv = Json.toJson(hdr)

    val respJv = JsObject(Map("header" -> hdrJv, "payload" -> emptyObject))
    Json.stringify(respJv)
  }

  def discoverAppliances(mqttClient: Mqtt, dAReqHeader: ReqHeader, context: Context): String = {

    var response = Option.empty[String]
    val logger = context.getLogger

    //Publish should not be necessary if we use a persistent message!
    logger.log("Subscribing to ListDevicesResponses")
    mqttClient.subscribe(LambdaHandler.topicListDevicesResponses, (topic, msg) => {
      logger.log("Received list devices response")
      response = Option(msg)
    })

    def timeIsUp(startTime: Long, seconds: Integer) = System.currentTimeMillis() - startTime > (1000 * seconds)

    //Wait 1 second for retained message to come through
    val startTime1 = System.currentTimeMillis()
    while(response.isEmpty && !timeIsUp(startTime1, 1)){
      Thread.sleep(1 * 1000)
    }


    //15 second timeout in AWS Lambda
    var haveRequested = false
    val startTime2 = System.currentTimeMillis()
    while(response.isEmpty && !timeIsUp(startTime2, 15)) {
      //Publish message and wait for response
      if(!haveRequested) {
        haveRequested = true
        mqttClient.publish(LambdaHandler.topicListDevicesRequests, "pls")
      }

      Thread.sleep(1000)
      logger.log("no response yet. Sleeping.")
    }

    logger.log(s"done waiting. response: $response")

    response match {
      case Some(respJs) => assembleAppliancesIntoResponse(respJs, dAReqHeader, context) match {
        case None => "could not assemble home skill response from published message"
        case Some(respObj) => {
          val respJv = Json.toJson(respObj)
          val resp = Json.stringify(respJv)
          resp
        }
      }
      case None =>  "did not get a response to device request"
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
