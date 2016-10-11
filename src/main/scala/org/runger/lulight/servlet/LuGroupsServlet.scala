package org.runger.lulight.servlet

import org.runger.lulight.{Logging, LuStack}

/**
  * Created by randy on 10/10/16.
  */

trait LuGroupsServlet extends Logging {
  self: LuStack =>

  info("Booting LuGroups servlet ")

  get("/quickGroups") {
    contentType = "text/html"
    scaml("quickGroups")
  }





}
