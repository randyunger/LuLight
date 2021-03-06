package org.runger.lulight

import org.joda.time.DateTime

/**
  *
  * Created by Unger on 2/18/16.
  *
  **/

object SceneSet {
  val meta = MetaConfig()
  import meta._

  //todo: These are no longer identical between local and prod
  val watchTv = Scene("Watch TV", Set(
     KitchenCans -> 0
    ,KitchenSpots-> 0
    ,KitchenIslandPendants -> 25
  ))

  val allOff = Scene("All Off", Set(
    KitchenCans -> 0
    ,KitchenIslandPendants -> 0
    ,KitchenSpots -> 0
    ,BreakfastCans -> 0
    ,BreakfastChandi -> 0
    ,BreakfastSconces -> 0
    ,MasterCansFront -> 0
    ,MasterRandy -> 0
    ,BreakfastCans -> 0
  ))

  val prodInstance: SceneSet = SceneSet(Set(
    watchTv
    ,allOff
  ))
  def apply() = prodInstance
}

case class SceneSet(scenes: Set[Scene]) {
  def get(label: String) = scenes.find(_.label == label)
}


//A Scene has its level stored in the State data of it's Lighting Loads
case class Scene(label: String, loads:LoadSet) extends Logging {
  def execute(excutor: String => Unit) = {
    val loadStates = loads.loads.map(load => {
      val level = load.state.map(_.level).getOrElse {
        warn("No state for scene load")
        0f
      }
      val cmd = load.set(level.toInt)
      excutor(cmd)
      LoadState(load.id, level, DateTime.now)
    })
    loadStates
  }
}

object Scene extends Logging {
  def apply(label: String, meta: Set[(LoadMeta, Int)]): Scene = {
    val confMap = LuConfig().storedConfig.byId

    val loads = meta.map{case (loadMeta, level) => {
      val load = confMap.getOrElse(loadMeta.luId, {
        warn(s"No lu config for meta $loadMeta")
        throw new IllegalArgumentException
      })
      load.withSetLevel(level)
    }}

    Scene(label, LoadSet(loads))
  }
}
