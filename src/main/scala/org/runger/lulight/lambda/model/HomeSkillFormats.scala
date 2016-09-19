package org.runger.lulight.lambda.model

import play.api.libs.json._

/**
  * Created by randy on 9/12/16.
  */


object HomeSkillFormats {

  implicit val dAReqHeaderFmt = Json.format[DAReqHeader]
  implicit val dAReqPayloadFmt = Json.format[DAReqPayload]
  implicit val discoverAppliancesRequestFmt = Json.format[DiscoverAppliancesRequest]

  implicit val daRespHeaderFmt = Json.format[DARespHeader]
  implicit val additionalApplianceDetailsFmt = Json.format[AdditionalApplianceDetails]
  implicit val applianceFmt = Json.format[Appliance]
  implicit val daRespPayloadFmt = Json.format[DARespPayload]
  implicit val discoverAppliancesResponseWrites = {
    //Json.format[DiscoverAppliancesResponse]
    Writes[DiscoverAppliancesResponse](dar => JsObject(Seq(
      "header" -> Json.toJson(dar.daRespHeader)
      ,"payload" -> Json.toJson(dar.dARespPayload)
    )))
  }
}

case class DAReqHeader(
                   messageId: String
                 ,name: String
                 ,namespace: String
                 ,payloadVersion: String
                 )


case class DAReqPayload(accessToken: String)

case class DiscoverAppliancesRequest(
                                    header: DAReqHeader
                                    ,payload: DAReqPayload
                                    )

case class DARespHeader(
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
                                     daRespHeader: DARespHeader
                                     ,dARespPayload: DARespPayload
                                     )