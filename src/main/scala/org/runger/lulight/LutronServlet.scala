package org.runger.lulight

import org.scalatra._
import play.api.libs.json.Json
import scalate.ScalateSupport
import Utils._

class LutronServlet extends LuStack with Logging {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

  get("/on/:id") {
    val id = params("id")
    val loads = LuConfig().search(id)
    loads.foreach (load =>{
      TelnetClient().execute(load.on())
    })
      loads.mkString("<br/>")
  }

  get("/off/:id") {
    val id = params("id")
    val loads = LuConfig().search(id)
    loads.foreach (load =>{
      TelnetClient().execute(load.off())
    })
    loads.mkString("<br/>")
  }

  post("/set/:id/:level") {
    val id = params("id")
    val levelO = params("level").tryToInt
    info(s"Setting id: $id to level: $levelO")

    val loads = LuConfig().search(id)

    loads.foreach(load => {
      levelO.foreach(level => {
        TelnetClient().execute(load.set(level))
      })
    })

    loads.mkString("<br/>")
  }

  get("/reload") {
    contentType = "application/text"
    LuConfig.reload()
  }

  get("/areas") {
    contentType="text/html"
    val areas = LuConfig().areas
    scaml("areas", "areas" -> areas)
  }

  get("/loads") {
    contentType="text/html"
    val loadSet = LuConfig()
    val byArea = LuConfig().loads.groupBy(_.areaName)
    val fullState = LuStateTracker().fullState(TelnetClient(), 3, 1000).toMap
    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}

    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
    scaml("loads", "loadSet" -> LuConfig(), "byArea" -> byArea, "fullStateJson" -> fullStateJson)
  }

  get("/state") {
    contentType = "application/json"

    val fullState = LuStateTracker().fullState(TelnetClient(), 3, 1000).toMap
    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}

    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
    fullStateJson
  }

}
