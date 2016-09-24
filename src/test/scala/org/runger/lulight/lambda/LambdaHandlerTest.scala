package org.runger.lulight.lambda

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{ClientContext, CognitoIdentity, Context, LambdaLogger}
import org.runger.lulight.lambda.model.{DiscoverAppliancesResponse, HomeSkillFormats}
import org.specs2.mutable.Specification
import play.api.libs.json.{JsString, JsValue, Json}

/**
  * Created by randy on 9/12/16.
  */
class LambdaHandlerTest extends Specification {

  val discoveryHeaderStr = """{
                             |    "header": {
                             |        "namespace": "Alexa.ConnectedHome.Discovery",
                             |        "name": "DiscoverAppliancesRequest",
                             |        "payloadVersion": "2",
                             |        "messageId": "60074682-798a-4621-a87c-a72325458969"
                             |    },
                             |    "payload": {
                             |        "accessToken": "AnythingCanBeAToken"
                             |    }
                             |}""".stripMargin


  def mkIS(content: String) = new ByteArrayInputStream(content.getBytes)
//  val discoveryIS = new ByteArrayInputStream(discoveryHeaderStr.getBytes)
  def mkOS = new ByteArrayOutputStream()

  val fakeContext = new Context {
    override def getIdentity: CognitoIdentity = ???

    override def getLogStreamName: String = ???

    override def getClientContext: ClientContext = ???

    override def getLogger: LambdaLogger = new LambdaLogger {
      override def log(string: String): Unit = {
        println(string)
      }
    }

    override def getMemoryLimitInMB: Int = ???

    override def getInvokedFunctionArn: String = ???

    override def getRemainingTimeInMillis: Int = ???

    override def getAwsRequestId: String = "FakeRequestId"

    override def getFunctionVersion: String = ???

    override def getFunctionName: String = ???

    override def getLogGroupName: String = ???
  }

  def osToString(os: ByteArrayOutputStream) = {
    os.toString("UTF-8")
  }

  "LambdaHandler" should {

    "accept mock objects" in {
      val lh = new LambdaHandler()
      lh.handleRequest(mkIS(discoveryHeaderStr), mkOS, fakeContext)
      ok
    }

    "return stock response" in {
      val lh = new LambdaHandler()
      val os = mkOS

      println(discoveryHeaderStr.replace("\n", ""))

      lh.handleRequest(mkIS(discoveryHeaderStr), os, fakeContext)

      val expectedS = """{
                       |    "header": {
                       |        "messageId": "myMessageID",
                       |        "name": "DiscoverAppliancesResponse",
                       |        "namespace": "Alexa.ConnectedHome.Discovery",
                       |        "payloadVersion": "2"
                       |    },
                       |    "payload": {
                       |        "discoveredAppliances": [
                       |            {
                       |                "actions": [
                       |                    "setPercentage",
                       |                    "incrementPercentage",
                       |                    "decrementPercentage",
                       |                    "turnOff",
                       |                    "turnOn"
                       |                ],
                       |                "additionalApplianceDetails": {},
                       |                "applianceId": "12345ID",
                       |                "friendlyDescription": "A fake appliance!",
                       |                "friendlyName": "Fakey",
                       |                "isReachable": true,
                       |                "manufacturerName": "CDMT",
                       |                "modelName": "LuLight1.0",
                       |                "version": "1.0"
                       |            }
                       |        ]
                       |    }
                       |}""".stripMargin

      val expectedJ = Json.parse(expectedS)
      val expectedPayload = expectedJ \ "payload"

      val actualJ = Json.parse(osToString(os))
      val actualPayload = actualJ \ "payload"

      actualPayload shouldEqual(expectedPayload)
    }

    "handle TurnOff requests" in {

      skipped //Since this will send a real mqtt message

      val action = """{
                     |    "header": {
                     |        "namespace": "Alexa.ConnectedHome.Control",
                     |        "name": "TurnOffRequest",
                     |        "payloadVersion": "2",
                     |        "messageId": "7fa86d2c-acc9-4c7b-8d43-8aa96bb2ac43"
                     |    },
                     |    "payload": {
                     |        "accessToken": "AnythingCanBeAToken",
                     |        "appliance": {
                     |            "applianceId": "60",
                     |            "additionalApplianceDetails": {}
                     |        }
                     |    }
                     |}""".stripMargin


      val turnOffConfirmation = """{
                                  |        "header": {
                                  |            "messageId": "26fa11a8-accb-4f66-a272-8b1ff7abd722",
                                  |            "name": "TurnOffConfirmation",
                                  |            "namespace": "Alexa.ConnectedHome.Control",
                                  |            "payloadVersion": "2"
                                  |        },
                                  |        "payload": {}
                                  |    }"""

      val os = mkOS
      println("pre handler")
      new LambdaHandler().handleRequest(mkIS(action), os, fakeContext)
      println("post handler")
      val actualJ = Json.parse(osToString(os))
      val actualHeader = (actualJ \ "header").get
      val actualPayload = (actualJ \ "payload").get

      def checkJv(jv: JsValue, key: String, value: String) = {
        val strVal = (jv \ key).get.as[JsString].value
        strVal shouldEqual (value)
      }

      checkJv(actualHeader, "namespace", "Alexa.ConnectedHome.Control")
      checkJv(actualHeader, "name", "TurnOffConfirmation")
      checkJv(actualHeader, "payloadVersion", "2")

      actualPayload shouldEqual(HomeSkillFormats.emptyObject)

    }
  }

}
