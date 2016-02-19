package org.runger.lulight

/**
  *
  * Created by Unger on 2/18/16.
  *
  **/

object SceneSet {
  import MetaConfig._

  val watchTv = Scene("Watch TV", Set(
     KitchenCans -> 0
    ,KitchenSpots-> 0
    ,KitchenIsland -> 25
  ))

  val prodInstance: SceneSet = SceneSet(Set(
    watchTv
  ))
  def apply() = prodInstance
}

case class SceneSet(scenes: Set[Scene]) {
  def get(label: String) = scenes.find(_.label == label)
}

case class Scene(label: String, loads:LoadSet) extends Logging {
  def execute(excutor: CommandExecutor) = {
    loads.loads.foreach(load => {
      val state = load.state.map(_.level).getOrElse {
        warn("No state for scene load")
        0f
      }
      val cmd = load.set(state.toInt)
      excutor.execute(cmd)
    })
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
