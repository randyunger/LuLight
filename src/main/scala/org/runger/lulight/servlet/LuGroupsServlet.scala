package org.runger.lulight.servlet

import org.runger.lulight._

/**
  * Created by randy on 10/10/16.
  */

trait LuGroupsServlet extends Logging {
  self: LuStack =>

  info("Booting LuGroups servlet ")

  get("/quickView") {
    contentType = "text/html"

    val allLoads = LuStateTracker().loadSet  //LuConfig().storedConfig.withState
    //    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap

    scaml("quickView", "allLoads" -> allLoads)
  }


  get("/quickGroups") {
    contentType = "text/html"

    val allLoads = LuStateTracker().loadSet  //LuConfig().storedConfig.withState
    val allGroups = Groups.all
    //    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap

    scaml("quickGroups", "allLoads" -> allLoads, "allGroups" -> allGroups)
  }
}
