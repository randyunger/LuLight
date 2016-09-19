package org.runger.lulight.lambda

/**
  * Created by randy on 9/11/16.
  */

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import org.runger.lulight.lambda.model._
import play.api.libs.json.{JsDefined, JsString, JsUndefined, Json}
import HomeSkillFormats._

class LambdaHandler extends RequestStreamHandler{

  def isFakeContext(context: Context): Boolean = {
    context.getAwsRequestId == "FakeRequestId"
  }


  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val logger = context.getLogger

    val p = Json.parse(input)
    val ostr = Json.asciiStringify(p)

    logger.log(s"Input received: $ostr")


    val nameRes = p \ "header" \ "name"
    logger.log(s"name: $nameRes")

//    val daReq = p.asOpt[DiscoverAppliancesRequest]
    val header = (p \ "header").as[DAReqHeader]

    val out = nameRes match {
      case JsDefined(JsString("DiscoverAppliancesRequest")) if isFakeContext(context) => discoverAppliancesFake(header) //todo: Remove
      case JsDefined(JsString("DiscoverAppliancesRequest")) => discoverAppliances(header)
      case JsDefined(JsString("otherTypeGoesHere")) => {"no response 2"} //todo
      case JsUndefined() => {"error 1"} //todo
    }

    logger.log(s"output will be: $out")
    output.write(out.getBytes("UTF-8"))
    output.flush()
  }

  val lightActions = List("setPercentage", "incrementPercentage", "decrementPercentage", "turnOff", "turnOn")
  val fakeDetails = AdditionalApplianceDetails(None, None, None, None)

  def reqHeaderToRespHeader(dAReqHeader: DAReqHeader): DARespHeader = {
    DARespHeader("myMessageID", "DiscoverAppliancesResponse", dAReqHeader.namespace, dAReqHeader.payloadVersion)
  }

  def discoverAppliances(dAReqHeader: DAReqHeader): String = {
    ""
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
