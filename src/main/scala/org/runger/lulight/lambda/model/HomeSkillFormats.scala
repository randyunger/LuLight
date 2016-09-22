package org.runger.lulight.lambda.model

import play.api.libs.json._

/**
  * Created by randy on 9/12/16.
  */


object HomeSkillFormats {

  val emptyObject = JsObject(Seq.empty)
  val emptyPayload = JsObject(Map("payload" -> emptyObject))

  implicit val dAReqHeaderFmt = Json.format[ReqHeader]
  implicit val dAReqPayloadFmt = Json.format[DAReqPayload]
  implicit val discoverAppliancesRequestFmt = Json.format[DiscoverAppliancesRequest]

  implicit val responseHeaderFmt = Json.format[ResponseHeader]
  implicit val additionalApplianceDetailsFmt = Json.format[AdditionalApplianceDetails]
  implicit val applianceFmt = Json.format[Appliance]
  implicit val daRespPayloadFmt = Json.format[DARespPayload]
  implicit val discoverAppliancesResponseWrites = {
    Writes[DiscoverAppliancesResponse](dar => JsObject(Seq(
      "header" -> Json.toJson(dar.daRespHeader)
      ,"payload" -> Json.toJson(dar.dARespPayload)
    )))
  }
}

case class ReqHeader(
                   messageId: String
                 ,name: String
                 ,namespace: String
                 ,payloadVersion: String
                 )


case class DAReqPayload(accessToken: String)

case class DiscoverAppliancesRequest(
                                    header: ReqHeader
                                    ,payload: DAReqPayload
                                    )

case class ResponseHeader(
                         messageId: String
                         ,name: String
                         ,namespace: String
                         ,payloadVersion: String
                       )

//case class ApplianceAction()

case class AdditionalApplianceDetails(
                                     extraDetail1: Option[String]
                                     ,extraDetail2: Option[String]
                                     ,extraDetail3: Option[String]
                                     ,extraDetail4: Option[String]
                                     )

case class Appliance(
                    actions: List[String] //todo could be enum
                    ,additionalApplianceDetails: AdditionalApplianceDetails
                    ,applianceId: String
                    ,friendlyDescription: String
                    ,friendlyName: String
                    ,isReachable: Boolean
                    ,manufacturerName:String
                    ,modelName: String
                    ,version: String
                    )

case class DARespPayload(
                        discoveredAppliances: List[Appliance]
                        )

case class DiscoverAppliancesResponse(
                                     daRespHeader: ResponseHeader
                                     ,dARespPayload: DARespPayload
                                     )