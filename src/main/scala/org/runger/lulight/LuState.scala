package org.runger.lulight

/**
 * Created by Unger on 11/28/15.
 */

import Utils._
import org.joda.time.DateTime
import play.api.libs.json.Json

object LoadState {
  implicit val loadStateFormat = Json.format[LoadState]
}

case class LoadState(id: Int, level: Float, timestamp: DateTime)

case class StateSignal(prefix: Char, typ: String, id: Int, action: Int, value: Float) {
  def toLoadState = LoadState(id, value, DateTime.now())
}

object StateSignal {
  def apply(line: String): Option[StateSignal] = {
    val sections = line.replace("GNET> ", "").split(',')
    if(sections.length < 3)
      None
    else {
      val prefix = sections(0).head
      val typ = sections(0).drop(1)
      for {
        id <- sections(1).tryToInt
        action <- sections(2).tryToInt
        if action == 1
        value <- sections(3).tryToFloat
      } yield StateSignal(prefix, typ, id, action, value)
    }
  }
}

object LuStateTracker extends Logging {
  def apply() = {
    prodState
  }

  val prodState = new LuStateTracker(LuConfig().storedConfig)
}

class LuStateTracker(config: LoadSet) extends Logging {
  val state = new ConcMap[LightingLoad, LoadState]()

  def withState(load: LightingLoad): LightingLoad = {
    val stO = Some(LoadState(load.id, (math.random*100).toFloat, DateTime.now()))
//    val st0 = state.get(load)

    //Keep old state if state not found in map
    val newState = stO orElse load.state
    load.copy(state = newState)
  }

  def update(line: String) = {
    val sig = StateSignal(line)

    sig match {
      case Some(signal) => {
        val st = signal.toLoadState
        val loadO = config.byId.get(st.id)
        loadO match {
          case Some(load) => {
            state += (load -> st)
            info(s"Updated state: ${load} is ${st}")
          }
          case None => warn(s"Load not found in config for line $line")
        }
      }
      case  None => {
        warn(s"Not able to interpret Telnet line: $line")
      }
    }
  }

  def unknownLoads() = {
    config.loads diff state.keySet
  }

  //todo: cache this
  def fullState(executor: String => Unit, tries: Int, delay: Int): ConcMap[LightingLoad, LoadState]  = {
    val unknown = unknownLoads()
    if (unknown.size > 0) {
      if(tries > 0) {
        unknown.foreach(load => {
          val cmd = load.getState()
//          client.execute(cmd)
          executor(cmd)
        })
        Thread.sleep(delay)
        fullState(executor, tries-1, delay)
      }
      else {
        info(s"Not able to get state within $tries tries")
        state
      }
    }
    else {
      info("returning full state")
      state
    }
  }
}
