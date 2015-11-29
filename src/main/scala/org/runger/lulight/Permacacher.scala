//package org.runger.lulight
//
//import akka.actor.Cancellable
//import collection.mutable
//import java.util.concurrent.atomic.AtomicLong
//import scala.concurrent.duration._
//
//// ################################################################################
////
////                 E V I C T A B L E   C A C H E
////
//// ################################################################################
//
//trait Logg {
//  val Logger = new Logging {}
//  def info(ss: String*) = Logger.info(ss.mkString(" ; "))
//  def trace(ss: String*) = Logger.info(ss.mkString(" ; "))
//  def warn(ss: String*) = Logger.warn(ss.mkString(" ; "))
//  def logError(ss: String*) = Logger.error(ss.mkString(" ; "))
//}
//
//case class Counter(a: String, b: String, c: Boolean, d: CounterType.types) {
//  def increment(): Unit = {}
//  def set(x: Any): Unit = {}
//}
//
//object CounterType {
//  trait types
//  case object AVERAGE extends types
//}
//
//object PermaCacher extends Logg {
//
//  import PermaCacherPsm._
//
//  //
//  // for logging
//  //
//  private[permacacher] val nameLogHead = "PermaCacher: "
//  private[permacacher] val troubleFetching = "trouble fetching asset for priming or refresh at key: {0}, thread {1}"
//  private[permacacher] val unableGetOrRegisterIntro = nameLogHead + troubleFetching
//  private[permacacher] val getOrRegisterWithOptionSome = nameLogHead + "Loading item {0} for first time, " +
//    "scheduling reload at interval of {1} seconds"
//  private[permacacher] val getOrRegisterWithOptionNone = nameLogHead + "Initial call to factory returned `None`! " +
//    "So `None` has been cached for key {0} but it will try to retrieve it again in {1} seconds."
//  private[permacacher] val unableGetOrRegisterWithOptionIntro = nameLogHead + " (with Option) " + troubleFetching
//  private[permacacher] val skipCachingMessage = nameLogHead+"unable to load key: {0} because Factory opined to disapprove"
//  private[permacacher] val badCachingMessage = nameLogHead+"(INTERNAL) retrieved StorageNOP for key {0}, item {1}"
//  private[permacacher] val evictionError = nameLogHead + "eviction attempt failed to cancel reload for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val evictionNopInfo = nameLogHead + "attempt to cancel non-existing scheduler for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val forceEvictionWarn = nameLogHead + "force attempt to evict for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val externalEvictionInfo = nameLogHead + "external eviction of key: {0} at UTC timestamp: {1}"
//  private[permacacher] val cleanupAfterPartial = nameLogHead + "cleaning up after partial Register: removing remnants for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val cannotStartEvictor = nameLogHead + "cannot start evictor yet, will retry later"
//  private[permacacher] val startedEvictor = nameLogHead + "evictor now started"
//  private[permacacher] val uncachedItemMessage = nameLogHead + "not cached key {0} with reload interval 0 (pass-through)"
//
//
//  //
//  // housekeeping
//  //
//  val timestampHistoryLength = 49
//  val cacheAccessTimeInfiniteTTL: Long = Long.MaxValue
//  val cacheAccessTimeNeverYet: Long = 0L
//  val isEnforceAssertions = false
//  private[permacacher] val cacheHitCounter = new Counter("Cache Hits", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher] val cachedItemsCounter = new Counter("Registered Items", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher] val evictableItemsCounter = new Counter("Evictable-only Registered", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher] val cachingFailureCounter = new Counter("Registeration Cleanups", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher] val passThroughCounter = new Counter("Pass-through (uncached)", "PermaCacher", true, CounterType.AVERAGE)
//
//  //
//  // shared caches; these are common to all the
//  // Actor(s) and the API function users
//  //
//
//  // private for testing
//  private[permacacher] val cache = new mutable.HashMap[String, (Any, PermaCacherMeta)] with mutable.SynchronizedMap[String, (Any, PermaCacherMeta)]
//  private[permacacher] val cacheLastAccess = new mutable.HashMap[String, AtomicLong] with mutable.SynchronizedMap[String, AtomicLong]
//  // cache, cacheLastAccess are accessed/updated together and must remain consistent.  Correct operation
//  // requires synchronization on a common, shared mutex.  To keep synchronization nimble we have a mutex
//  // per key.  cacheKeyMutex protects the atomicity of [cache.get(key)/cacheLastAccess.put(key,timestamp)]
//  // so that, say, key will not disappear from cache, yet cacheLastAccess still puts a new ts
//  // ** ALWAYS acquire BEFORE collectiveMutex **
//  private[permacacher] val cacheKeyMutex = new mutable.HashMap[String, AnyRef] with mutable.SynchronizedMap[String, AnyRef]
//
//  //
//  // scheduled reloads; this is shared within and
//  // common among the API function users
//  //
//
//  // private for testing
//  private[permacacher] val reloadOps = new mutable.HashMap[String, Cancellable]() with mutable.SynchronizedMap[String, Cancellable]
//  // this additional mutex is needed when doing certain CRUD ops for reloadOps involving also cache and
//  // cacheLastAccess consistency granularity is coarser (and accordingly simpler to execute and keep track
//  // of) because occurrences of use are not that frequent
//  // ** ALWAYS acquire AFTER cacheKeyMutex **
//  private[permacacher] val collectiveMutex = new AnyRef()
//
//  // the next line starts the evictor system; if no evictor wanted (i.e. PermaCacher will behave
//  // as an immutable cache, same as what original behavior it manifested, then comment it out.
//  // You can check the counters page to verify at runtime if there is any eviction behavior for
//  // the currently running deployment
//  private[this] var isEvictorStarted = startEvictor
//
//  private[this] def startEvictor: Boolean = Option(cacheLastAccess) match {
//    case Some(_) => {
//      info (startedEvictor)
//      PermaCacherEvictor.triggerStart
//    }
//    case None => {
//      warn(cannotStartEvictor)
//      false
//    }
//  }
//
//  //
//  // DRY code normalization (NOT general purpose)
//  //
//
//  private[this] def collectivePrimeAndRegister[T]
//  (key: String, initialValue: T, reloadInSeconds: Long, mayBeEvicted: Boolean, reloader: Reloader) {
//
//    if (!isEvictorStarted) isEvictorStarted = startEvictor
//
//    if (0 != reloadInSeconds) {
//      // the following makes available the correct implicit ExecutionContext, taking care of this error:
//      // Cannot find an implicit ExecutionContext, either require one yourself or import ExecutionContext.Implicits.global
//      import PermaCacherAgent.refresherSystem.dispatcher
//
//      //
//      // ONLY => while holding cacheKeyMutex <=
//      // also acquires collectiveMutex in here
//      // DRY refactor: NOT a general purpose utility, beware
//      //
//      collectiveMutex.synchronized {
//
//        //
//        // HOLDING collectiveMutex
//        //
//
//        cache.put(key, (initialValue, PermaCacherMeta(reloadInSeconds)))
//        primeLastAccess(key, mayBeEvicted)
//        cachedItemsCounter.increment()
//        reloadOps.put(key, PermaCacherAgent.refresherSystem.scheduler.schedule(reloadInSeconds seconds, reloadInSeconds seconds,
//          PermaCacherAgent.refresherBalancer, reloader))
//      }
//    } else {
//      warn(uncachedItemMessage,key)
//    }
//  }
//
//  private[this] def collectiveConsistencyCheck(key: String) {
//
//    //
//    // ONLY => while holding cacheKeyMutex <=
//    // also acquires collectiveMutex in here
//    // DRY refactor: NOT a general purpose utility, beware
//    //
//
//    collectiveMutex.synchronized {
//
//      //
//      // HOLDING collectiveMutex
//      //
//
//      val allOk = cache.contains(key) && cacheLastAccess.contains(key) && reloadOps.contains(key)
//      if (!allOk) {
//        warn(cleanupAfterPartial, key, System.currentTimeMillis().toString)
//        cachingFailureCounter.increment()
//        cache.remove(key)
//        cacheLastAccess.remove(key)
//        reloadOps.remove(key)
//        cacheKeyMutex.remove(key)
//      }
//    } // collectiveMutex.synchronized
//  }
//
//  private[this] def updateLastAccess(key: String) = {
//
//    //
//    // ONLY => while holding cacheKeyMutex <=
//    // DON'T acquire ANY mutex in here
//    // DRY refactor: NOT a general purpose utility, beware
//    //
//
//    cacheLastAccess.get(key) match {
//      case Some(ts) =>
//        if (cacheAccessTimeInfiniteTTL != ts.get())
//          ts.set(System.currentTimeMillis())
//      case None =>
//        logError(catTimestampMissing, key, System.currentTimeMillis().toString)
//        throw new IllegalStateException(illegalStateMsg)
//    }
//  }
//
//  private[this] def primeLastAccess(key: String, mayBeEvicted: Boolean) = {
//
//    //
//    // ONLY => while holding cacheKeyMutex <=
//    // so DON'T acquire ANY mutex in here
//    // DRY refactor: NOT a general purpose utility, beware
//    //
//
//    val timeStamp = if (mayBeEvicted) {
//      evictableItemsCounter.increment()
//      cacheAccessTimeNeverYet
//    } else cacheAccessTimeInfiniteTTL
//    cacheLastAccess.put(key, new AtomicLong(timeStamp))
//  }
//
//  //
//  // utility methods/functions
//  //
//
//  private[permacacher] def metaOrDefault(key: String, reloadInSeconds: Long) = PermaCacher.cache.get(key).map {
//    case (value, meta) => meta
//  }.getOrElse(PermaCacherMeta(reloadInSeconds))
//
//
//  // this will evict key no-matter-what, i.e. it is the burden of the
//  // caller to establish business rules if key should be evicted.  There
//  // is no business logic here.  Key, short of an error or other trouble,
//  // will be evicted.  Only concern of relevance is the consistency of
//  // the state machine.
//  private[permacacher] def evict(key: String, force: Boolean = false): Boolean = {
//
//    val stateBefore = if (isEnforceAssertions) {
//      val (isConsistent, state) = isConsistentBeforeEvict(key)
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//      state
//    }
//
//    // in case of corruption for cacheKeyMutex, this **may** be the way out
//    val ckMxHolder = if (force) {
//      warn(forceEvictionWarn,key,System.currentTimeMillis().toString)
//      Some(new AnyRef())
//    } else cacheKeyMutex.get(key)
//
//    val isSuccess = ckMxHolder match {
//      case Some(ckMx) => ckMx.synchronized {
//
//        //
//        // HOLDING cacheKeyMutex
//        //
//
//        collectiveMutex.synchronized {
//
//          //
//          // HOLDING collectiveMutex
//          //
//
//          val isSchedulerCancelled = try {
//            reloadOps.get(key) match {
//              case Some(sched) =>
//                sched.cancel()
//                reloadOps.remove(key)
//              case None => info(evictionNopInfo, key, System.currentTimeMillis().toString)
//            }
//            // the scheduler will NOT run again here for key, one hopes.
//            // at this point there should be no further attempt to update cache,
//            // unless key is re-registered right here, which is possible unless
//            // reloadOps is ALWAYS protected using collectiveMutex
//            true
//          } catch {
//            case ex: Exception =>
//              logError(ex.getStackTraceString, evictionError, key, System.currentTimeMillis().toString)
//              false
//          }
//          cache.remove(key)
//          cacheLastAccess.remove(key)
//          isSchedulerCancelled
//        } // collectiveMutex.synchronized
//      } // cacheKeyMutex.synchronized
//      case None => collectiveMutex.synchronized {
//
//        //
//        // HOLDING collectiveMutex
//        //
//
//        info(catKeyMutexMsg, key, System.currentTimeMillis().toString)
//        val isRemnants = reloadOps.contains(key) || cache.contains(key) || cacheLastAccess.contains(key)
//        !isRemnants
//      }
//    } //
//    if (isSuccess) cacheKeyMutex.remove(key)
//
//    if (isEnforceAssertions) {
//      val isConsistent = isConsistentAfterEvict(key,stateBefore.asInstanceOf[PsmState])
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//    }
//
//    isSuccess
//  }
//
//  // =============================================================================================
//  // this is the public API to IntelligenceSchemaServlet, strictly read-only
//  // =============================================================================================
//
//  def display: Iterable[PermaCacherDisplayView] = {
//    cache.map {
//      case (key, (value, meta)) => PermaCacherDisplayView(key, value, PermaCacherMetaView(meta))
//    }
//  }
//
//  def numberOfTimers: Int = {
//    reloadOps.size
//  }
//
//  //  def mailboxSize: String = {
//  //
//  //    // the following makes available the correct implicit ActorSystem, taking care of this error:
//  //    // could not find implicit value for parameter system: akka.actor.ActorSystem
//  //    import PermaCacherAgent.refresherSystem
//  //
//  //    PermaCacherAgent.refresherActors.toList.map(MeteredMailboxExtension.getMailboxSize(_)).mkString("[", ",", "]")
//  //  }
//
//
//  // =============================================================================================
//  // listener public API, enables registration of listeners, else strictly read-only.  Listeners
//  // MUST NOT mess with the internal state of PermaCacher or all bets are off.  Hopefully they
//  // cannot...
//  // =============================================================================================
//
//  val listeners = new mutable.HashSet[PermaCacherListener]() with mutable.SynchronizedSet[PermaCacherListener]
//
//  // =============================================================================================
//  // general purpose public API, read-write
//  // =============================================================================================
//
//  //
//  // Dumps the cache and restarts the scheduler
//  //
//  // protected [intelligence] for testing does not work as expected...
//  def restart() = {
//
//    if (isEnforceAssertions) {
//      if (!isConsistentBeforeRestart) throw new IllegalStateException(illegalStateMsg)
//    }
//
//    // in lieu of acquiring, destroy.
//    // May create errors in log, but
//    // at this point, who cares...
//    cacheKeyMutex.clear()
//
//    collectiveMutex.synchronized {
//
//      //
//      // HOLDING collectiveMutex
//      //
//
//      for (reloadOp <- reloadOps) {
//        reloadOp._2.cancel()
//      }
//      // the scheduler will NOT run again here, one hopes,
//      // since reloadOps is protected with collectiveMutex and
//      // therefore cannot be reinitialized asynchronously
//      reloadOps.clear()
//      // at this point there should be no further attempt to
//      // update cache, so the next line is the last word
//      cache.clear()
//      // same here
//      cacheLastAccess.clear()
//    }
//
//    // just in case something happened
//    // asynchronously in the meantime
//    cacheKeyMutex.clear()
//
//    if (isEnforceAssertions) {
//      if (!isConsistentAfterRestart) throw new IllegalStateException(illegalStateMsg)
//    }
//  }
//
//  def get[T](key: String): Option[T] = {
//
//    val stateBefore = if (isEnforceAssertions) {
//      val (isConsistent, state) = isConsistentBeforeGet(key)
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//      state
//    }
//
//    val retVal = cacheKeyMutex.get(key) match {
//      case Some(o) =>
//        o.synchronized {
//
//          //
//          // HOLDING cacheKeyMutex
//          //
//
//          cache.get(key) match {
//            case Some((value, _)) =>
//              cacheHitCounter.increment()
//              updateLastAccess(key)
//              Some(value.asInstanceOf[T])
//            case None =>
//              logError(catCacheMissing, key, System.currentTimeMillis().toString)
//              throw new IllegalStateException(illegalStateMsg)
//          }
//        }
//      case None => None
//    }
//
//    if (isEnforceAssertions) {
//      val isConsistent = isConsistentAfterGet(key,stateBefore.asInstanceOf[PsmState])
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//    }
//
//    retVal
//  }
//
//  def clearResultFromCache(key: String): Boolean = {
//    info(externalEvictionInfo,key,System.currentTimeMillis().toString)
//    evict(key)
//  }
//
//  def shutdown(): Unit = {
//    PermaCacherAgent.refresherSystem.shutdown()
//  }
//
//  def getOrRegisterConditional[T](key: String, reloadInSeconds: Long, mayBeEvicted: Boolean = false)
//                                 (factory : (Option[T]) => StorageResult[T]): Option[T] = {
//    getOrRegisterConditional(key, factory, reloadInSeconds)
//  }
//
//  /**
//   * Will retrieve an item from the cache.  If the item does not exist, it will insert the item with a scheduled reload
//   * after allowing the factory an opportunity to compare the current cache content vs. the new spawn from persistent
//   * storage.  If the new spawn does appear, say, corrupted, then the factory may opine not to refresh, but rather to
//   * reduce the refresh operation to a NOP and leave the cache content unaltered.  StorageResult case class is the
//   * mechanism allowing the opining factoring to impress on PermaCacher what the course of action shall be.  Therefore,
//   * PermaCacherAgent (see) becomes a dumb and faithful agent plainly carrying out the factory's all-knowing decision.
//   */
//  def getOrRegisterConditional[T](key: String, factory: (Option[T]) => StorageResult[T],
//                                  reloadInSeconds: Long, mayBeEvicted: Boolean = false): Option[T] = {
//
//    val stateBefore = if (isEnforceAssertions) {
//      val (isConsistent,state) = isConsistentBeforeGetOrRegister(key)
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//      state
//    }
//
//    // here, reloadOp does not evaluate factory
//    val reloadOp = factory
//
//    val lockHolder = cacheKeyMutex.getOrElseUpdate(key, new AnyRef())
//    var isNOP = false
//
//    // cacheKeyMutex ALWAYS acquired BEFORE collectiveMutex
//    val retVal = lockHolder.synchronized {
//
//      //
//      // HOLDING cacheKeyMutex
//      //
//
//      val aux = cache.get(key) match {
//        case Some((result, _)) => {
//          updateLastAccess(key)
//          cacheHitCounter.increment()
//          val retVal = result match {
//            case a@Some(_) => a
//            case None => None
//            case b => Some(b)
//          }
//          retVal.asInstanceOf[Option[T]]
//        }
//        case None => try {
//          listeners.foreach(_.onBeforeKeyUpdated(key))
//          // if factory blows, it will do so here
//          val vvv = factory(None) match {
//            case StorageRefresh(itemToBeStored) => {
//              collectivePrimeAndRegister(key, itemToBeStored, reloadInSeconds, mayBeEvicted, ReloadOpConditional(key, reloadOp ,reloadInSeconds))
//              (Some(itemToBeStored),itemToBeStored)
//            }
//            case nop@StorageNOP => {
//              warn(skipCachingMessage,key)
//              isNOP = true
//              // collectiveConsistencyCheck(key) with cleanup will happen in the finally{} clause
//              (None,None)
//            }
//          }
//
//          listeners.foreach(_.onAfterKeyUpdated(key, Some(vvv._2)))
//          // case nop@StorageNOP() lost type info, so need to provide here again when None
//          vvv._1.asInstanceOf[Option[T]]
//        } catch {
//          case ex: Exception =>
//            listeners.foreach(_.onError(key, ex))
//            logError(ex.getStackTraceString, unableGetOrRegisterIntro, key, Thread.currentThread().getName)
//            throw ex
//        } finally {
//          collectiveConsistencyCheck(key)
//        } // case None with try-catch-finally
//      } // cache.get(key) match
//      aux
//    } // cacheKeyMutex.synchronized
//
//    if (isEnforceAssertions && 0!=reloadInSeconds) {
//      val isConsistent = if (isNOP) {
//        isConsistentBeforeGetOrRegister(key)._1
//      } else {
//        isConsistentAfterGetOrRegister(key, stateBefore.asInstanceOf[PsmState])
//      }
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//    }
//
//    retVal
//  }
//
//  def getOrRegister[T](key: String, reloadInSeconds: Long, mayBeEvicted: Boolean = false)
//                      (factory: => T): T = getOrRegister(key, factory, reloadInSeconds, mayBeEvicted)
//
//  /**
//   * Will retrieve an item from the cache.  If the item does not exist, it will insert the item with a scheduled reload.
//   */
//  def getOrRegister[T](key: String, factory: => T,
//                       reloadInSeconds: Long, mayBeEvicted: Boolean = false): T = {
//
//    val stateBefore = if (isEnforceAssertions) {
//      val (isConsistent,state) = isConsistentBeforeGetOrRegister(key)
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//      state
//    }
//
//    // here, reloadOp does not evaluate factory
//    val reloadOp = () => factory
//
//    val lockHolder = cacheKeyMutex.getOrElseUpdate(key, new AnyRef())
//
//    // cacheKeyMutex ALWAYS acquired BEFORE collectiveMutex
//    val retVal = lockHolder.synchronized {
//
//      //
//      // HOLDING cacheKeyMutex
//      //
//
//      val aux = cache.get(key) match {
//        case Some((result, _)) =>
//          updateLastAccess(key)
//          cacheHitCounter.increment()
//          result.asInstanceOf[T]
//        case None => try {
//          listeners.foreach(_.onBeforeKeyUpdated(key))
//          // if factory blows, it will do so here
//          val initialValue = factory
//          collectivePrimeAndRegister(key, initialValue, reloadInSeconds, mayBeEvicted, ReloadOp(key, reloadOp, reloadInSeconds))
//          listeners.foreach(_.onAfterKeyUpdated(key, Some(initialValue)))
//          initialValue
//        } catch {
//          case ex: Exception =>
//            listeners.foreach(_.onError(key, ex))
//            logError(ex.getStackTraceString, unableGetOrRegisterIntro, key, Thread.currentThread().getName)
//            throw ex
//        } finally {
//          collectiveConsistencyCheck(key)
//        } // case None with try-catch-finally
//      } // cache.get(key) match
//      aux
//    } // cacheKeyMutex.synchronized
//
//    if (isEnforceAssertions && 0!=reloadInSeconds) {
//      val isConsistent = isConsistentAfterGetOrRegister(key, stateBefore.asInstanceOf[PsmState])
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//    }
//
//    retVal
//  }
//
//  def getOrRegisterWithOption[T](key: String, reloadInSeconds: Long, mayBeEvicted: Boolean = false)
//                                (factory: => Option[T]): Option[T] = getOrRegisterWithOption(key, factory, reloadInSeconds, mayBeEvicted)
//
//  /**
//   * Will attempt to retrieve an item from the cache.  If the item does not exist,
//   * it will call the factory method and set it with the key if Some[T] is the factory result
//   */
//
//  def getOrRegisterWithOption[T](key: String, factory: => Option[T],
//                                 reloadInSeconds: Long, mayBeEvicted: Boolean = false): Option[T] = {
//
//    val stateBefore = if (isEnforceAssertions) {
//      val (isConsistent,state) = isConsistentBeforeGetOrRegister(key)
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//      state
//    }
//
//    // here, reloadOp does not evaluate factory
//    val reloadOp = () => factory
//
//    val lockHolder = cacheKeyMutex.getOrElseUpdate(key, new AnyRef())
//
//    // cacheKeyMutex ALWAYS acquired BEFORE collectiveMutex
//    val retVal = lockHolder.synchronized {
//
//      //
//      // HOLDING cacheKeyMutex
//      //
//
//      val aux = cache.get(key) match {
//        case Some((result, _)) =>
//          updateLastAccess(key)
//          cacheHitCounter.increment()
//          // for getOrRegisterWitOption of getOrRegister item
//          val retVal = result match {
//            case a@Some(_) => a
//            case None => None
//            case b => Some(b)
//          }
//          retVal.asInstanceOf[Option[T]]
//        case None => try {
//          listeners.foreach(_.onBeforeKeyUpdated(key))
//          val refresher = ReloadOptionally(key, reloadOp, reloadInSeconds)
//          // mayhem, if any, would likely happen with factory
//          val result = factory match {
//            case initialValue@Some(vvv) =>
//              info(getOrRegisterWithOptionSome, key, reloadInSeconds.toString)
//              collectivePrimeAndRegister(key, initialValue, reloadInSeconds, mayBeEvicted, refresher)
//              initialValue
//            case None =>
//              info(getOrRegisterWithOptionNone, key, reloadInSeconds.toString)
//              collectivePrimeAndRegister(key, None, reloadInSeconds, mayBeEvicted, refresher)
//              None
//          }
//
//          listeners.foreach(_.onAfterKeyUpdated(key, result))
//          result
//        } catch {
//          case ex: Exception =>
//            listeners.foreach(_.onError(key, ex))
//            logError(ex.getStackTraceString, unableGetOrRegisterWithOptionIntro, key, Thread.currentThread().getName)
//            throw ex
//        } finally {
//          collectiveConsistencyCheck(key)
//        } // case None with try-catch-finally
//      } // match
//      aux
//    } // cacheKeyMutex.synchronized
//
//    if (isEnforceAssertions && 0!=reloadInSeconds) {
//      val isConsistent = isConsistentAfterGetOrRegister(key, stateBefore.asInstanceOf[PsmState])
//      if (!isConsistent) throw new IllegalStateException(illegalStateMsg)
//    }
//
//    retVal
//  } // getOrRegisterWithOption
//
//}
//
////
//// listener API, enables registration of listeners
////
//
//trait PermaCacherListener {
//
//  def onBeforeKeyUpdated(key: String)
//
//  def onAfterKeyUpdated[T](key: String, value: Option[T])
//
//  def onError(key: String, complaint: Throwable)
//}
//
///**
// * Created by runger on 2/20/14.
// */
//
//import java.util.concurrent.atomic.AtomicLong
//import akka.actor.{Cancellable, Props, ActorSystem, Actor}
//import org.joda.time.format.DateTimeFormat
//import com.typesafe.config.ConfigFactory
//import scala.collection.{immutable, mutable}
//import scala.concurrent.duration._
//
//// ##############################################################################
////
////                    TTL and eviction refactoring
////
//// Since PermaCacher is not (almost) read-only any more, but is now a fully fledged
//// read-write-evict cache, a snake has entered eden and must be dealt with.  Near-
//// immutability and its intrinsic thread-safety has gone the way of innocence.
////
//// There is an embedded state machine built from the state of the following Maps:
//// cache, cacheLastAccess, cacheKeyMutex, reloadOps
////
//// ------------------------------------------------------------------------------
//// Their internal collective state must remain consistent at all times.  Here are
//// all possible collectively-internally-consistent states.
//// ------------------------------------------------------------------------------
////
//// LEGEND:
//// EMPTY  globally empty, i.e. isEmpty==true
//// K-val  there is some value for key K
//// K-nil  there is no value for key K (i.e. contains(K) == false)
//// K-not  last access time is "never yet" (well-known constant)
//// K-inf  last access time is "infinite TTL" (well-known constant)
//// K-ts   last access time is an actual timestamp
//// K-here lock object for key K exists
//// K-fact factory function for key K exists
////
////
//// ** global start state
//// (EMPTY)      cache EMPTY, cacheLastAccess EMPTY, cacheKeyMutex EMPTY,  reloadOps EMPTY
////
//// ** initial state for evictable key
//// (KEY-INIT-E) cache K-val, cacheLastAccess K-not, cacheKeyMutex K-here, reloadOps K-fact
////
//// ** generic state for persistent key
//// (KEY-P)      cache K-val, cacheLastAccess K-inf, cacheKeyMutex K-here, reloadOps K-fact
////
//// ** generic state for evictable key
//// (KEY-E)      cache K-val, cacheLastAccess K-ts,  cacheKeyMutex K-here, reloadOps K-fact
////
//// ** removed (i.e. after eviction)
//// (REMOVE)     cache K-nil, cacheLastAccess K-nil, cacheKeyMutex K-nil,  reloadOps K-nil
////
//// ------------------------------------------------------------------------------
//// here are all verbs to move from one state to another
//// ------------------------------------------------------------------------------
//// (public) restart
//// (public) get(key) <== NOT readonly! may change last-access timestamp
//// (public) getOrRegister [with or without Option, irrelevant here] in short getOrR
//// (asynchronous) refresh [i.e. refresh from persistent storage] in short refresh
//// (asynchronous) evict
////
//// ------------------------------------------------------------------------------
//// here are all legal transitions
//// = is the start, and < or > is the end of a transition. A transition with arrows on
//// both ends indicates that no change in state occurred.
//// ------------------------------------------------------------------------------
////    EMPTY    KEY-INIT-E   KEY-P      KEY-E     REMOVED
////      =          =          =          =          =
////      =          =          =          =          =
////      =          =          =          =          =
////      V          V          V          V          V
////
////                                ## get family ##
////
//// 1|<--get->|
//// 2    =--getOrR-->
//// 3    =--getOrR------------->
//// 4               =--getOrR------------->
//// 5               =--get---------------->
//// 6                      |<--get->|
//// 7                      |<getOrR>|
//// 8                                 |<--get->|
//// 9                                 |<getOrR>|
//// A                                            |<--get->|
//// B                          <-----------getOrR----=
//// C               <----------------------getOrR----=
////
////                                   ## refresh ##
////
//// D           |<rfrsh->|
//// E                      |<rfrsh->|
//// F                                 |<rfrsh->|
////
////                                   ## restart ##
////
//// G    <----------=--restart--=---------=----------=       (4 transitions)
//// H|<restart>|
////
////                                    ## evict ##
////
//// I               =---evict---=---------=---------->       (3 transitions)
//// J    <----------=---evict---=---------=                  (3 transitions)
//// K|<-evict>|
//// L                                            |<-evict>|
////
//// This table lays out 28 transitions
////
//// ##############################################################################
//
//
//// ################################################################################
////
////                  S T A T E    M A C H I N E
////
//// ################################################################################
//
//sealed abstract class PsmState(key : String, n : Int) { def id = n }
//case class EmptyPsmState(empty : String) extends PsmState(empty, 1) // EMPTY
//case class KeyInitEvictablePsmState(eKeyInit : String) extends PsmState(eKeyInit, 2) // KEY-INIT-E
//case class KeyEvictablePsmState(eKey : String) extends PsmState(eKey, 3) // KEY-E
//case class KeyPermanentPsmState(pKey : String) extends PsmState(pKey, 4) // KEY-P
//case class RemovedPsmState(removed: String) extends PsmState(removed, 5) // REMOVED
//
//object PermaCacherPsm extends Logg {
//
//  import PermaCacher.cache
//  import PermaCacher.cacheLastAccess
//  import PermaCacher.cacheKeyMutex
//  import PermaCacher.reloadOps
//  import PermaCacher.collectiveMutex
//  import PermaCacher.cacheAccessTimeNeverYet
//  import PermaCacher.cacheAccessTimeInfiniteTTL
//
//  //
//  // State consistency enforcement
//  //
//
//  private[permacacher] val nameLogHead = "PermaCacherPsm: "
//  private[permacacher] val catKeyMutexMsg = nameLogHead + "entry in cacheKeyMutex not found for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val catKeyMutexMissing = nameLogHead + "(INTERNAL) missing (expected but not found) entry in cacheKeyMutex for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val catKeyMutexStray = nameLogHead + "(INTERNAL) stray (found but not expected) entry in cacheKeyMutex for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val catTimestampMissing = nameLogHead + "(INTERNAL) missing (expected but not found) entry in cacheLastAccess for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val catTimestampStray = nameLogHead + "(INTERNAL) stray (found but not expected) entry in cacheLastAccess for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val catCacheMissing = nameLogHead + "(INTERNAL) missing (expected but not found) entry in cache for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val catCacheStray = nameLogHead + "(INTERNAL) stray (found but not expected) entry in cache for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val catFactoryMissing = nameLogHead + "(INTERNAL) missing (expected but not found) entry in reloadOps for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val catFactoryStray = nameLogHead + "(INTERNAL) stray (found but not expected) entry in reloadOps for key: {0} at UTC timestamp: {1}"
//  private[permacacher] val illegalStateMsg = nameLogHead + "(INTERNAL) embedded state machine is corrupt"
//
//  private[permacacher] def getPsmState( key : String ) : PsmState = {
//
//
//
//    def getPsmState_(key : String, isInCache : Boolean, isInCkm : Boolean, isInReloadOps: Boolean, timeStamp : Option[AtomicLong]) : PsmState = {
//      val retVal : PsmState = if (isInCache && isInCkm && isInReloadOps) timeStamp match {
//        case Some(ts) =>
//          if (cacheAccessTimeNeverYet == ts.get()) KeyInitEvictablePsmState(key)
//          else if (cacheAccessTimeInfiniteTTL == ts.get()) KeyPermanentPsmState(key)
//          else KeyEvictablePsmState(key)
//        case None =>
//          logError(catTimestampMissing,key,System.currentTimeMillis.toString)
//          throw new IllegalStateException(illegalStateMsg)
//      } else if (!(isInCache || isInCkm || isInReloadOps)) timeStamp match {
//        case Some(ts) =>
//          logError(catTimestampStray,key,System.currentTimeMillis.toString)
//          throw new IllegalStateException(illegalStateMsg)
//        case None =>
//          if (cache.isEmpty && cacheLastAccess.isEmpty && cacheKeyMutex.isEmpty && reloadOps.isEmpty) EmptyPsmState(key)
//          else RemovedPsmState(key)
//      } else {
//        if (isInCache) {
//          logError(catCacheStray,key,System.currentTimeMillis.toString)
//        } else if (isInCkm) {
//          logError(catKeyMutexStray,key,System.currentTimeMillis.toString)
//        } else if (isInReloadOps) {
//          logError(catFactoryStray,key,System.currentTimeMillis.toString)
//        } else timeStamp match {
//          case Some(ts) =>
//            logError(catTimestampStray,key,System.currentTimeMillis.toString)
//            throw new IllegalStateException(illegalStateMsg)
//          case None =>
//        }
//        throw new IllegalStateException(illegalStateMsg)
//      }
//      retVal
//    }
//
//    val ckmHolder = cacheKeyMutex.get(key)
//    ckmHolder match {
//      case Some(ckm) => ckm.synchronized {
//        collectiveMutex.synchronized {
//          getPsmState_(key, cache.contains(key), isInCkm = true, reloadOps.contains(key), cacheLastAccess.get(key))
//        }
//      }
//      case None => collectiveMutex.synchronized {
//        getPsmState_(key, cache.contains(key), isInCkm = false,reloadOps.contains(key), cacheLastAccess.get(key))
//      }
//    }
//  }
//
//  private[permacacher] def isPmsStateEmpty : Boolean = {
//    collectiveMutex.synchronized {
//      cache.isEmpty && cacheLastAccess.isEmpty && cacheKeyMutex.isEmpty && reloadOps.isEmpty
//    }
//  }
//
//  // ---------------------------------------------------------------------------------
//  // ** get ** transition contracts
//  //
//  // get can start from any state
//  private[permacacher] def isConsistentBeforeGet(key : String) : (Boolean, PsmState) = (true, getPsmState(key))
//  // after get
//  // we are never in KEY-INIT-E
//  // else we are always in the same state as before get
//  private[permacacher] def isConsistentAfterGet(key : String, stateBefore : PsmState) : Boolean = {
//    val currState = getPsmState(key)
//    val isNever = currState match {
//      case KeyInitEvictablePsmState(_) => false
//      case _ => true
//    }
//    val isState = if (stateBefore.id == KeyInitEvictablePsmState(key).id) {
//      KeyEvictablePsmState(key).id == currState.id
//    } else {
//      stateBefore.id == currState.id
//    }
//    isNever && isState
//  }
//
//  // ---------------------------------------------------------------------------------
//  // ** getOrRegister ** transition contracts
//  //
//  // getOrRegister can start from any state
//  private[permacacher] def isConsistentBeforeGetOrRegister(key : String) : (Boolean, PsmState) = (true, getPsmState(key))
//  // after getOrRegister
//  // we are never in EMPTY or REMOVED (*)
//  // if start is KEY-P or KEY-E then after is same state
//  // if start is EMPTY then after is KEY-INIT-E or KEY-P
//  // if start is REMOVED then after is KEY-INIT-E or KEY-P
//  // if start is KEY-INIT-E then after is KEY-E
//  //
//  // (*) unless there was an error on the first getOrRegister ever
//  private[permacacher] def isConsistentAfterGetOrRegister(key : String, stateBefore : PsmState) : Boolean = {
//    val currState = getPsmState(key)
//    val isNever = currState match {
//      case EmptyPsmState(_) => false
//      case RemovedPsmState(_) => false
//      case _ => true
//    }
//    val isState = stateBefore match {
//      case KeyEvictablePsmState(_) => stateBefore.id == currState.id
//      case KeyPermanentPsmState(_) => stateBefore.id == currState.id
//      case EmptyPsmState(_) => currState.id == KeyInitEvictablePsmState(key).id || currState.id == KeyPermanentPsmState(key).id
//      case RemovedPsmState(_) => currState.id == KeyInitEvictablePsmState(key).id || currState.id == KeyPermanentPsmState(key).id
//      case KeyInitEvictablePsmState(_) => currState.id == KeyEvictablePsmState(key).id
//    }
//    isNever && isState
//  }
//
//  // ---------------------------------------------------------------------------------
//  // ** refresh ** transition contracts
//  //
//  // before refresh
//  // we are never in EMPTY or REMOVED
//  private[permacacher] def isConsistentBeforeRefresh(key : String) : (Boolean, PsmState) = {
//    val currState = getPsmState(key)
//    val isConsistent = currState match {
//      case EmptyPsmState(_) => false
//      case RemovedPsmState(_) => false
//      case _ => true
//    }
//    (isConsistent, currState)
//  }
//  // after refresh:
//  // we are never in EMPTY or REMOVED
//  // else we are always in the same state as before refresh
//  private[permacacher] def isConsistentAfterRefresh(key : String, stateBefore : PsmState) : Boolean = {
//    val currState = getPsmState(key)
//    val isNever = currState match {
//      case EmptyPsmState(_) => false
//      case RemovedPsmState(_) => false
//      case _ => true
//    }
//    val isState = stateBefore.id == currState.id
//    isNever && isState
//  }
//
//  // ---------------------------------------------------------------------------------
//  // ** restart ** transition contracts
//  //
//  // restart can start from any state
//  private[utilities] val isConsistentBeforeRestart : Boolean = {
//    true
//  }
//  // after restart
//  // we are identically in EMPTY
//  private[utilities] def isConsistentAfterRestart : Boolean = {
//    isPmsStateEmpty
//  }
//
//  // ---------------------------------------------------------------------------------
//  // ** evict ** transition contracts
//  //
//  // evict can start from any state
//  private[permacacher] def isConsistentBeforeEvict(key : String) : (Boolean, PsmState) = (true, getPsmState(key))
//  // after evict
//  // if start is EMPTY then after is EMPTY
//  // if start is REMOVED then after is REMOVED
//  // if start is KEY-INIT-E or KEY-E or KEY-P then after is REMOVED or EMPTY
//  private[permacacher] def isConsistentAfterEvict(key : String, stateBefore : PsmState) : Boolean = {
//    val currState = getPsmState(key)
//    val isDestination = currState match {
//      case RemovedPsmState(_) => true
//      case EmptyPsmState(_) => true
//      case _ => false
//    }
//    val isState = stateBefore match {
//      case KeyEvictablePsmState(_) => currState.id == EmptyPsmState(key).id || currState.id == RemovedPsmState(key).id
//      case KeyPermanentPsmState(_) => currState.id == EmptyPsmState(key).id || currState.id == RemovedPsmState(key).id
//      case KeyInitEvictablePsmState(_) => currState.id == EmptyPsmState(key).id || currState.id == RemovedPsmState(key).id
//      case EmptyPsmState(_) => currState.id == stateBefore.id
//      case RemovedPsmState(_) => currState.id == stateBefore.id
//    }
//    isDestination && isState
//  }
//}
//
//// ################################################################################
////
////               A S Y N C H R O N O U S   R E F R E S H E R
////
//// ################################################################################
//
//sealed abstract class Reloader()
//protected case class ReloadOp(key: String, reloadOp: () => Any, reloadInSeconds: Long) extends Reloader()
//protected case class ReloadOpConditional[T](key: String, reloadOp: Option[T] => StorageResult[T], reloadInSeconds: Long) extends Reloader()
//protected case class ReloadOptionally(key: String, reloadOp: () => Option[Any], reloadInSeconds: Long) extends Reloader()
//
//sealed abstract class StorageResult[+T]
//case object StorageNOP extends StorageResult[Nothing]
//case class StorageRefresh[T]( itemToBeStored : T ) extends StorageResult[T]
//
//protected object PermaCacherAgent {
//
//  private[permacacher]  val actorNameLogHead = "PermaCacherAgent: "
//  private[permacacher]  val strangeness = actorNameLogHead + " received strangeness"
//  private[permacacher]  val reloadingMessageIntro = actorNameLogHead + " reloading key: {0}"
//  private[permacacher]  val reloadedMessageIntro = actorNameLogHead + " reloaded key: {0}"
//  private[permacacher]  val unableMessageIntro = actorNameLogHead + " unable to reload key: {0}"
//  private[permacacher]  val reloadingOptionIntro = actorNameLogHead + " reloading with Option for key: {0}"
//  private[permacacher]  val reloadedOptionIntro = actorNameLogHead + " reloaded with Option for key: {0}"
//  private[permacacher]  val reloadNoneIntro = actorNameLogHead + " reloading with None!" +
//    " The previous value is being overwritten with `None` for key: {0}"
//  private[permacacher]  val unableOptionIntro = actorNameLogHead + " with Option, unable to reload key: {0}"
//  private[permacacher]  val unableConditionalRetrieve = actorNameLogHead + " with Conditional, unable to examine key: {0} " +
//    "(attempting recovery with None)"
//  private[permacacher]  val skipCachingMessage = actorNameLogHead+"unable to reload key: {0} because Factory opined " +
//    "to disapprove"
//  private[permacacher]  val replaceAt0 = "{0}".length
//  private[permacacher]  val unableOptionIntroShort = unableOptionIntro.dropRight(replaceAt0)
//  private[permacacher]  val unableMessageIntroShort = unableMessageIntro.dropRight(replaceAt0)
//  private[permacacher]  val reloadCounter = new Counter("Reloads", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher]  val reloadNoneCounter = new Counter("Reload None", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher]  val reloadRejectCounter = new Counter("Reload Rejections", "PermaCacher", true, CounterType.AVERAGE)
//
//  val refresherSystemConf = ConfigFactory.load(ConfigFactory.parseString(
//    """  akka {
//           daemonic = on
//           actor {
//             default-dispatcher {
//               type = Dispatcher
//#               mailbox-type = "com.gravity.utilities.grvakka.UnboundedMeteredMailboxType"
//             }
//           }
//         }"""))
//
//  val refresherActorSystemName = "PermaCacherRefresher"
//  val refresherConcurrentActors = 3
//
//  // i.e. Hbase connections - 2; discussed with Chris, this is pretty critical so change only knowingly
//  //
//  // Akka actor pool setup for refresh
//  //
//  implicit val refresherSystem = ActorSystem(refresherActorSystemName, refresherSystemConf)
//  // protected  for testing
//  private[permacacher]  val refresherActors = 0 until refresherConcurrentActors map (_ => refresherSystem.actorOf(Props[PermaCacherAgent]))
//  private[permacacher]  val refresherBalancer = refresherSystem.actorOf(Props.empty.withRouter(akka.routing.SmallestMailboxRouter(refresherActors)))
//
//}
//
//protected class PermaCacherAgent extends Actor with Logg {
//
//  import PermaCacherAgent._
//  import PermaCacher.listeners
//  import PermaCacher.metaOrDefault
//  import PermaCacher.cache
//  import PermaCacher.collectiveMutex
//
//  def receive = {
//    case ReloadOp(key, reloadOperation, reloadInSeconds) =>
//      try {
//        trace(reloadingMessageIntro, key)
//        listeners.foreach(_.onBeforeKeyUpdated(key))
//
//        val meta = metaOrDefault(key, reloadInSeconds)
//        // does not mess with last-access time, etc. so the implicit
//        // lock on cache will be enough for this op
//        val result = cache.put(key, (reloadOperation(), meta.withCurrentTimestamp))
//        reloadCounter.increment()
//
//        listeners.foreach(_.onAfterKeyUpdated(key, result))
//        trace(reloadedMessageIntro, key)
//
//      } catch {
//        case ex: Exception =>
//          listeners.foreach(_.onError(key, ex))
//          logError(ex.getStackTraceString, unableMessageIntro, key)
//      }
//
////    case ReloadOpConditional(key, reloadOperation, reloadInSeconds) =>
////      try {
////        trace(reloadingMessageIntro, key)
////        listeners.foreach(_.onBeforeKeyUpdated(key))
////
////        val meta = metaOrDefault(key, reloadInSeconds)
////        // does not mess with last-access time, etc. so
////        // the implicit lock on cache will be enough
////        val previous = cache.get(key).map(_._1)
////        val result = reloadOperation(previous) match {
////          case StorageRefresh(vvv) =>
////            cache.put(key, (vvv, meta.withCurrentTimestamp))
////            reloadCounter.increment()
////            Some(vvv)
////          case StorageNOP =>
////            warn(skipCachingMessage, key, "")
////            reloadRejectCounter.increment()
////            None
////        }
////        listeners.foreach(_.onAfterKeyUpdated(key, result))
////        result match {
////          case Some(_) => trace(reloadedMessageIntro, key)
////          case None => trace(unableMessageIntro, key)
////        }
////
////      } catch {
////        case ex: Exception =>
////          listeners.foreach(_.onError(key, ex))
////          logError(ex.getStackTraceString, unableMessageIntro, key)
////      }
//
//    case ReloadOptionally(key, reloadOperation, reloadInSeconds) =>
//      try {
//        trace(reloadingOptionIntro, key)
//        listeners.foreach(_.onBeforeKeyUpdated(key))
//
//        val meta = metaOrDefault(key, reloadInSeconds)
//        // does not mess with last-access time, etc. so the implicit
//        // lock on cache will be enough for this op
//        val result = reloadOperation() match {
//          case s@Some(v) =>
//            reloadCounter.increment()
//            cache.put(key, (s, meta.withCurrentTimestamp))
//          case None =>
//            warn(reloadNoneIntro, key)
//            reloadNoneCounter.increment()
//            cache.put(key, (None, meta.withCurrentTimestamp))
//        }
//
//        listeners.foreach(_.onAfterKeyUpdated(key, result))
//        trace(reloadedOptionIntro, key)
//
//      } catch {
//        case ex: Exception =>
//          listeners.foreach(_.onError(key, ex))
//          logError(ex.getStackTraceString, unableOptionIntro, key)
//      }
//
//    case _ => warn(strangeness)
//
//  }
//
//}
//
//// ################################################################################
////
////               A S Y N C H R O N O U S   E V I C T O R
////
//// ################################################################################
//
//sealed abstract class PceTimeToLive()
//protected case class PceTimeToLiveCheckOp[V]
//(lastAccess : mutable.Map[String,AtomicLong], msTtl: Long = PermaCacherEvictor.msTimeToLiveDefault) extends PceTimeToLive()
//
//private[permacacher]  object PermaCacherEvictor {
//
//  //
//  // private  because the evicting behavior is completely optional,
//  // and depends on the initialization (i.e. instantiation) of this private
//  // object (which may or may not happen).  Limiting visibility to
//  // encapsulates within the package any instantiation of PermaCacherEvictor
//  // and therefore enables easier control of the overall behavior
//  //
//
//  private[permacacher]  val actorNameLogHead = "PermaCacherEvictor: "
//  private[permacacher]  val strangeness = actorNameLogHead + "received strangeness"
//  private[permacacher]  val nowRunning = actorNameLogHead + "timed eviction check at UTC {0}"
//  private[permacacher]  val troubleMessage = actorNameLogHead + "trouble during timed eviction check at UTC {0}"
//  private[permacacher]  val evictionMessage = actorNameLogHead + "timed eviction of key {0} at UTC {1}"
//  private[permacacher]  val neverAccessedMessage = actorNameLogHead + "found now-expired never-accessed key {0}"
//  private[permacacher]  val nowExamined = actorNameLogHead + "timed eviction check completed for {0} cached items"
//  private[permacacher]  val evictionCounter = new Counter("Timed Evictions", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher]  val unusedCounter = new Counter("Never-used Evictions", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher]  val unusedYetCounter = new Counter("Never-used Yet", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher]  val runCounter = new Counter("Eviction Check Runs", "PermaCacher", true, CounterType.AVERAGE)
//  private[permacacher]  val firstFound = new mutable.HashMap[String, Long] with mutable.SynchronizedMap[String, Long]
//
//  private[permacacher]  val evictorSystemConf = ConfigFactory.load(ConfigFactory.parseString(
//    """  akka {
//           daemonic = on
//           actor {
//             default-dispatcher {
//               type = Dispatcher
//               mailbox-type = "akka.dispatch.UnboundedMailbox"
//             }
//           }
//         }"""))
//
//  private[permacacher]  val evictorActorSystemName = "PermaCacherEvictor"
//  private[permacacher]  val evictorConcurrentActor = 1 // do not increase
//  private[permacacher]  val msInTwoHours = 1000*60*60*2
//  private[permacacher]  val sInTenMinutes = 60*10
//  private[permacacher]  val msTimeToLiveDefault = msInTwoHours
//  private[permacacher]  val scheduledRunInterval = sInTenMinutes
//
//  //
//  // Akka actor pool setup for evict
//  //
//  private[permacacher]  implicit val evictorSystem = ActorSystem(evictorActorSystemName, evictorSystemConf)
//  // the following makes available the correct implicit ExecutionContext, taking care of this error:
//  // Cannot find an implicit ExecutionContext, either require one yourself or import ExecutionContext.Implicits.global
//  private[permacacher]  implicit val executionContext = evictorSystem.dispatcher
//
//  // protected  for testing
//  private[permacacher]  val evictorActors = immutable.Seq(evictorSystem.actorOf(Props[PermaCacherEvictor]))
//  private[permacacher]  val evictorRouter = evictorSystem.actorOf(Props.empty.withRouter(akka.routing.RoundRobinRouter(evictorActors)))
//
//  private[permacacher]  def hasExpired(timeNow : Long, ttlVal : Long)(timeStamp : Long) = (timeNow-timeStamp) > ttlVal
//  private[permacacher]  def scheduleTtlCheckOp(msTtl : Long) : Cancellable = PermaCacherEvictor.evictorSystem.scheduler.
//    schedule(scheduledRunInterval seconds, scheduledRunInterval seconds,PermaCacherEvictor.evictorRouter,
//      PceTimeToLiveCheckOp(PermaCacher.cacheLastAccess, msTtl))
//
//  // this will start the scheduler with default ttl **if and when**  evaluated
//  private[permacacher]  val ttlDefaultCheckOpScheduler = scheduleTtlCheckOp(msTimeToLiveDefault)
//
//  // dummy hook to trigger instantiation
//  private[this] var beenHere = false
//  private[permacacher]  def triggerStart : Boolean = {
//    /*
//     Scala 2.10 Spec para. 5.4, Object Definition
//     "Note that the value defined by an object definition is instantiated lazily. [... It is only ]
//     evaluated the first time [...] dereferenced during execution of the program (which might be
//     never at all)."
//     */
//    if (!beenHere) {
//      beenHere = true
//      println("PermaCacherEvictor instantiated")
//    }
//    beenHere
//  }
//}
//
//protected class PermaCacherEvictor extends Actor with Logg {
//
//  import PermaCacherEvictor._
//
//  def receive = {
//
//    case PceTimeToLiveCheckOp(tsLiveMap, ttlVal) =>
//      try {
//        runCounter.increment()
//        val tsMap : immutable.Map[String,AtomicLong] = tsLiveMap.toMap
//        val timeNow = System.currentTimeMillis()
//        def isExpired = hasExpired(timeNow, ttlVal) _
//        info(nowRunning, timeNow.toString)
//        for ((key, ts) <- tsMap) {
//          val myTs = ts.get()
//          val isEvict = if (myTs == PermaCacher.cacheAccessTimeInfiniteTTL) false
//          else {
//            if (myTs == PermaCacher.cacheAccessTimeNeverYet) {
//              val foundTs = firstFound.getOrElseUpdate(key,timeNow)
//              if (isExpired(foundTs)) {
//                firstFound.remove(key)
//                info(neverAccessedMessage, key)
//                unusedCounter.increment()
//                true // never accessed and now expired
//              }
//              else false // never accessed yet, not expired
//            } else {
//              firstFound.remove(key)
//              isExpired(myTs)
//            }
//          }
//          if (isEvict) {
//            PermaCacher.evict(key)
//            evictionCounter.increment()
//            info(evictionMessage, key, timeNow.toString)
//          }
//        }
//        unusedYetCounter.set(firstFound.size)
//        info(nowExamined, tsMap.size.toString)
//      } catch {
//        case ex: Exception =>
//          logError(ex.getStackTraceString, troubleMessage, System.currentTimeMillis().toString)
//      }
//
//    case _ => warn(strangeness)
//
//  }
//
//}
//
//// ################################################################################
////
////               M E T A D A T A    R E P O R T E R
////
//// ################################################################################
//
//protected case class PermaCacherMeta(timestamps: Seq[Long], reloadInSeconds: Long) {
//  require(0 <= reloadInSeconds)
//
//  def withCurrentTimestamp = {
//    val now = System.currentTimeMillis()
//    // if d is the time delta between now and the last time used (say 3 seconds, i.e.
//    // this cache entry was last queried 3 seconds ago), and r is the max allowed
//    // staleness between reloads, we have case (1) d > r or case (2) d < r
//    // if (1) [say, r is 2 seconds] then delay is 1 second (positive)
//    // if (2) [say, r is 5 seconds] then delay is -2 seconds (negative)
//    // when delay is a negative number, there is no problem; if delay
//    // is positive, delay means "lateness"
//    // Probe: ========= (uncomment next two lines) =========
//    // println("now:"+now+", reloadInSeconds (as ms):"+(reloadInSeconds*1000)+
//    //   ", ts:"+timestamps.head+", now-ts:"+(now -timestamps.head))
//    val delayms = timestamps.headOption.map(ts => now - ts - reloadInSeconds * 1000)
//    // Increment a counter if delay surpasses our grace period
//    // this is to create an audit record of the general trend
//    //    delayms.filter(d => d.toDouble > PermaCacherMeta.gracePeriodMillis).foreach(_ => PermaCacherMeta.ctr(PermaCacherMeta.delayCounterLabel))
//    val truncated = timestamps.take(PermaCacher.timestampHistoryLength)
//    PermaCacherMeta(now +: truncated, reloadInSeconds)
//  }
//}
//
//protected object PermaCacherMeta {// extends Counters {
////  override val counterCategory = "PermaCacherMeta"
//val delayCounterLabel = "delays"
//  val gracePeriodMillis = 200
//
//  def apply(reloadInSeconds: Long): PermaCacherMeta = {
//    val currMillis = System.currentTimeMillis()
//    PermaCacherMeta(Seq(currMillis), reloadInSeconds)
//  }
//}
//
////
//// Admin Page pretty-printer
////
//
//case class PermaCacherMetaView(timestamps: Seq[String], reloadInSeconds: Long, nextLoad: String, millisUntilReload: Long)
//
//object PermaCacherMetaView {
//  val dtf = DateTimeFormat.forPattern("HH:mm:ss a")
//
//  def apply(meta: PermaCacherMeta): PermaCacherMetaView = {
//    val timeStrings = meta.timestamps.map(dtf.print)
//    val nextLoad = meta.timestamps.headOption.map(ts => dtf.print(ts + meta.reloadInSeconds * 1000)).getOrElse("Could not determine last load time")
//    val millisUntilReload = meta.timestamps.headOption.map(ts => (ts - System.currentTimeMillis()) + meta.reloadInSeconds * 1000).getOrElse(Long.MinValue)
//    PermaCacherMetaView(timeStrings, meta.reloadInSeconds, nextLoad, millisUntilReload)
//  }
//}
//
//case class PermaCacherDisplayView(key: String, value: Any, meta: PermaCacherMetaView)
