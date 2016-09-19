package org.runger.lulight.lambda

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{ClientContext, CognitoIdentity, Context, LambdaLogger}
import org.specs2.mutable.Specification
import play.api.libs.json.Json

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
      override def log(string: String): Unit = {}
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
      val actualJ = Json.parse(osToString(os))

      actualJ shouldEqual(expectedJ)
    }
  }

}
