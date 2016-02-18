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
    val loads = LuConfig().state.search(id)
    loads.foreach (load =>{
      CommandExecutor().execute(load.on())
    })
      loads.mkString("<br/>")
  }

  get("/off/:id") {
    val id = params("id")
    val loads = LuConfig().state.search(id)
    loads.foreach (load =>{
      CommandExecutor().execute(load.off())
    })
    loads.mkString("<br/>")
  }

  post("/set/:id/:level") {
    val id = params("id")
    val levelO = params("level").tryToInt
    info(s"Setting id: $id to level: $levelO")

    val loads = LuConfig().state.search(id)

    loads.foreach(load => {
      levelO.foreach(level => {
        CommandExecutor().execute(load.set(level))
      })
    })

    loads.mkString("<br/>")
  }

  get("/reload") {
    contentType = "application/text"
    LuConfig().reload()
  }

  get("/areas") {
    contentType="text/html"
    val areas = LuConfig().state.areas
    scaml("areas", "areas" -> areas)
  }

  get("/loads") {
    contentType="text/html"
    val loadSet = LuConfig().state
    val byArea = LuConfig().state.loads.groupBy(_.areaName)
    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}

    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
    scaml("loads", "loadSet" -> LuConfig().state, "byArea" -> byArea, "fullStateJson" -> fullStateJson)
  }

  get("/state") {
    contentType = "application/json"

    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}

    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
    fullStateJson
  }

  get("/down") {
    contentType="text/html"
//    val byBulb = LuConfig().state.loads.filter(_.isDown)//.map(_.meta)
//      .groupBy(_.meta.map(_.bulb))

//    val b = for {
//      load <- LuConfig().state.loads
//      if load.isDown
//      meta <- load.meta
////      if meta.bulb ==
//    } yield (load, meta)
//
//    val byBulb = b.groupBy(_._2.bulb).map{
//      case(k, v) => (k.toString, v.map(_._1))
//    }
    val filterSet = FilterSet(floor = Some(Floor.Downstairs))

    val byArea = LuConfig().state.loads.filter(_.isDown).groupBy(_.areaName)

    val bulbTypes = (for {
      ll <- byArea.values.flatten
      llM <- ll.meta
    } yield llM.bulb).toSet

    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}

    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
    val filterSetJson = Json.asciiStringify(Json.toJson(filterSet))

    scaml("loads2", "byArea" -> byArea, "bulbTypes" -> bulbTypes, "filterSetJson" -> filterSetJson,"fullStateJson" -> fullStateJson)
  }

  get("/up") {
    contentType="text/html"
    val byArea = LuConfig().state.loads.filter(_.isUp).groupBy(_.areaName)

    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}

    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
    scaml("loads2", "byArea" -> byArea, "fullStateJson" -> fullStateJson)
  }

  post("/filtered") {
    contentType = "application/json"

    val req = request
    val levelP = params("level")
    val level = levelP.tryToInt.getOrElse {
      warn("Couldn't parse level for filtered")
      throw new IllegalArgumentException
    }
    val filters = request.body
    val x = request.getParameter("")
    val y = multiParams
    info(y.toString)
    val yf = y.filter{case (k,v) => k!="level"}
    val json = yf.head._1
    info(json)
    info(s"multi: $filters")
    val jv = Json.parse(json)
    val ff = Json.fromJson[FilterSet](jv)
    info(ff.toString)
    val filterSet = ff.asOpt.getOrElse {
      info("Couldn't parse filters")
      throw new IllegalArgumentException
    }
    val loads = LuConfig().state.filterBy(filterSet)
    loads.loads.foreach(load => {
      CommandExecutor().execute(load.set(level))
    })
  }
//  get("/downInc") {
//    contentType="text/html"
//    val byArea = LuConfig().state.loads.filter(_.isUp).groupBy(_.areaName)
//
//    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
//    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}
//
//    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
//    scaml("loads2", "byArea" -> byArea, "fullStateJson" -> fullStateJson)
//  }


}
