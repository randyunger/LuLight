package org.runger.lulight

/**
 * Created by Unger on 11/28/15.
 */

import java.util.concurrent.ConcurrentHashMap

import scala.reflect.ClassTag

//based on a combination of the overlock project (https://github.com/boundary/overlock), which had Far More than We Needed, and wasn't maintained, and https://gist.github.com/brikis98/5843195
class ConcMap[A, B: ClassTag](initialCapacity: Int = 16, loadFactor: Float = 0.75f, concurrencyLevel: Int = 16)
  extends scala.collection.concurrent.Map[A, B] {

  private class ConcMapValueWrapper[A: ClassTag](op: => A) {
    lazy val value = op

    override def equals(other: Any) = other match {
      case o: ConcMapValueWrapper[_] => value == o.value
      case v: A => value == v
      case _ => false
    }
  }

  private val internalMap = new ConcurrentHashMap[A, ConcMapValueWrapper[B]](initialCapacity, loadFactor, concurrencyLevel)

  override def -=(key: A): this.type = {
    internalMap.remove(key)
    this
  }

  override def +=(kv: (A, B)): this.type = {
    internalMap.put(kv._1, new ConcMapValueWrapper(kv._2))
    this
  }

  override def iterator: Iterator[(A, B)] = {
    new Iterator[(A, B)] {
      val iter = internalMap.entrySet().iterator()

      def next(): (A, B) = {
        val entry = iter.next()
        (entry.getKey, entry.getValue.value)
      }

      def hasNext = iter.hasNext
    }
  }

  override def get(key: A): Option[B] = Option(internalMap.get(key)).map(_.value)

  override def replace(k: A, v: B): Option[B] = Option(internalMap.replace(k, new ConcMapValueWrapper(v))).map(_.value)

  override def replace(k: A, oldvalue: B, newvalue: B): Boolean = internalMap.replace(k, new ConcMapValueWrapper(oldvalue), new ConcMapValueWrapper(newvalue))

  override def remove(k: A, v: B): Boolean = internalMap.remove(k, new ConcMapValueWrapper(v))

  override def putIfAbsent(k: A, v: B): Option[B] = Option(internalMap.putIfAbsent(k, new ConcMapValueWrapper(v))).map(_.value)

  override def getOrElseUpdate(k: A, op: => B): B = {
    val t = new ConcMapValueWrapper(op)
    Option(internalMap.putIfAbsent(k, t)).getOrElse(t).value
  }
}


