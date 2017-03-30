package org.runger.lulight.graph

import java.util.UUID

/**
  * Created by randy on 1/9/17.
  */

//case class LoadRow(id: UUID, projectId: UUID, localId: Int,  displayName: String, roomName: String,
//                   fixtureType: String, privacyStatus: String, bulbType: String)


case class LoadNode (id: String, localId: Int,  displayName: String, roomName: String,
                   fixtureType: String, privacyStatus: String, bulbType: String) {

}
