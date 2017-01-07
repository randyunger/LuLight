package org.runger.lulight

/**
  * Created by randy on 10/11/16.
  */

object Groups {
  val meta = MetaConfig()
  import meta._

  val all = GroupSet(Set(
    Group("All Lights", meta.meta.toSet, GroupSet.empty)
  ))

}

object GroupSet {
  val empty = GroupSet(Set.empty)
}

case class GroupSet(groups: Set[Group]) {
  def byName = groups.toList.sortBy(_.name)
}

case class Group(name: String, loadMeta: Set[LoadMeta], groupSet: GroupSet)

