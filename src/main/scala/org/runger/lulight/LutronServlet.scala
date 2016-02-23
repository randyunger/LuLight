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
    val loads = LuConfig().storedConfig.search(id)
    loads.foreach (load =>{
      CommandExecutor().execute(load.on())
    })
      loads.mkString("<br/>")
  }

  get("/off/:id") {
    val id = params("id")
    val loads = LuConfig().storedConfig.search(id)
    loads.foreach (load =>{
      CommandExecutor().execute(load.off())
    })
    loads.mkString("<br/>")
  }

  post("/set/:id/:level") {
    val id = params("id")
    val levelO = params("level").tryToInt
    info(s"Setting id: $id to level: $levelO")

    val loads = LuConfig().storedConfig.search(id)

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
    val areas = LuConfig().storedConfig.areas
    scaml("areas", "areas" -> areas)
  }

  get("/loads") {
    contentType="text/html"
    val loadSetWState = LuConfig().storedConfig.withState
    val byArea = loadSetWState.loads.groupBy(_.areaName)
//    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
//    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}
//
//    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
    scaml("loads", "loadSet" -> LuConfig().storedConfig, "byArea" -> byArea)
  }

  get("/state") {
    contentType = "application/json"
    info("returning full state from API")
    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}

    if(fullStateById.isEmpty) {
      warn("Not able to return status!")
      status(500)
    } else {
      val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
      fullStateJson
    }
  }

  get("/down") {
    contentType="text/html"
    val filterSet: FilterSet = FilterSet(floors = Set(Floor.Downstairs))

    val sceneSet = SceneSet()

//    val loadSetWState = LuConfig().storedConfig.filterBy(filterSet).withState
    val loadSetWState = filterSet.filter(LuConfig().storedConfig)
    val byArea = loadSetWState.loads.groupBy(_.areaName)
//    val byArea = LuConfig().storedConfig.loads.filter(_.isDown).groupBy(_.areaName)

    val bulbTypes = (for {
      ll <- byArea.values.flatten
      llM <- ll.meta
    } yield llM.bulb).toSet

//    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
//    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}
//    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))

    val filterSetJson = Json.asciiStringify(Json.toJson(filterSet))

    scaml("loads2", "sceneSet" -> sceneSet, "byArea" -> byArea, "bulbTypes" -> bulbTypes, "filterSetJson" -> filterSetJson)
  }

  get("/up") {
    contentType="text/html"
    val filterSet = FilterSet(floors = Set(Floor.Upstairs))

    val sceneSet = SceneSet()

    val byArea = LuConfig().storedConfig.loads.filter(_.isUp).groupBy(_.areaName)

    val bulbTypes = (for {
      ll <- byArea.values.flatten
      llM <- ll.meta
    } yield llM.bulb).toSet

    val fullState = LuStateTracker().fullState(CommandExecutor().execute, 3, 1000).toMap
    val fullStateById = fullState.map{ case(k, v) => (k.id.toString, v)}

    val fullStateJson = Json.asciiStringify(Json.toJson(fullStateById))
    val filterSetJson = Json.asciiStringify(Json.toJson(filterSet))

    scaml("loads2", "byArea" -> byArea, "sceneSet" -> sceneSet, "bulbTypes" -> bulbTypes, "filterSetJson" -> filterSetJson,"fullStateJson" -> fullStateJson)
  }

  post("/filtered") {
    contentType = "application/json"

//    val req = request
    val levelP = params("level")
    val level = levelP.tryToInt.getOrElse {
      warn("Couldn't parse level for filtered")
      throw new IllegalArgumentException
    }
//    val filters = request.body
//    val x = request.getParameter("")
//    val y = multiParams
//    info(y.toString)
    val yf = multiParams.filter{case (k,v) => k!="level"}
    val json = yf.head._1
    info(json)
//    info(s"multi: $filters")
    val jv = Json.parse(json)
    val ff = Json.fromJson[FilterSet](jv)
    info(ff.toString)
    val filterSet = ff.asOpt.getOrElse {
      info("Couldn't parse filters")
      throw new IllegalArgumentException
    }
//    val loads = LuConfig().storedConfig.filterBy(filterSet)
    val loads = filterSet.filter(LuConfig().storedConfig)
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

  post("/scene/:sceneName") {
    contentType = "application/json"
    val sn = params("sceneName")
    info(s"triggering scene for $sn")

    val scene = SceneSet().get(sn).getOrElse {
      warn(s"Could not find scene: $sn")
      throw new IllegalArgumentException
    }

    info(s"executing scene ${scene.label}")
    val loadStates = scene.execute(CommandExecutor().execute)

    val jv = Json.toJson(loadStates)
    val json = Json.asciiStringify(jv)

    json
  }

}
