package org.clulab.fatdynet.utils

import edu.cmu.dynet.ComputationGraph

object Synchronizer {
  protected val debug = false
  protected var count = 0
  protected var inSynchronized = false

  // Allow public query of the read-only version.
  def isSynchronized: Boolean = inSynchronized

  def log(count: Int, stage: String, startVersionOpt: Option[Long], message: Any): Unit = {
    val threadId = Thread.currentThread.getId
    val version = startVersionOpt.map(_.toString).getOrElse("?")
    println(s"Synchronizer\tcount\tstage\tthreadId\tversion\tmessage")
    println(s"Synchronizer\t$count\t$stage\t$threadId\t$version\t${message.toString}")
  }

  def before(message: Any, startVersionOpt: Option[Long]): Int = {
    // It is possible for the same thread to re-enter the synchronized section.  Avoid that!
    assert(!inSynchronized)
    inSynchronized = true
    val startCount = count
    count += 1 // Something else will see a different count now.
    if (debug)
      log(startCount, "before", startVersionOpt, message)
    startCount
  }

  def after(message: Any, startCount: Int, startVersionOpt: Option[Long], endVersionOpt: Option[Long]): Unit = {
    if (debug)
      log(startCount, "after", startVersionOpt, message)
    require(startVersionOpt == endVersionOpt, "ComputationGraph version should not change")
  }

  def withComputationGraph[T](message: Any)(f: => T): T = {
    // In parallel version, synchronize on Thread.currentThread.
    Synchronizer.synchronized {
      val startVersion = Some(ComputationGraph.version)
      val startCount = before(message, startVersion)
      try {
        val result = f // This needs to make all the nodes
        result
      }
      catch {
        case throwable: Throwable =>
          throwable.printStackTrace()
          throw throwable
      }
      finally {
        val endVersion = Some(ComputationGraph.version)
        after(message, startCount, startVersion, endVersion)
        // Make sure the nodes are freed immediately with clear().  This prevents live object
        // from being trashed and may help prevent memory fragmentation.
        // However, the line is redundant because ComputationGraph.renew() calls
        // delete immediately and there is no wait for garbage collection.
        // ComputationGraph.clear()
        ComputationGraph.renew()
        // Wait for the rest to disappear during finalization which need not be synchronized.
        inSynchronized = false
      }
    }
  }

  def withoutComputationGraph[T](message: Any)(f: => T): T = {
    // Synchronization here should be global.  There should be no active ComputationGraphs.
    Synchronizer.synchronized {
      val startVersion = None
      val startCount = before(message, startVersion)
      try {
        val result = f
        result
      }
      catch {
        case throwable: Throwable =>
          throwable.printStackTrace()
          throw throwable
      }
      finally {
        val endVersion = None
        after(message, startCount, startVersion, endVersion)
        // Make sure there is a ComputationGraph now as long as we're synchronized and
        // this typically runs before DyNet can be used.  It is otherwise possible
        // that the first graph is constructed when a model loads, without synchronization.

        // See https://stackoverflow.com/questions/9443137/rules-of-initializing-companion-object-values-in-scala.
        // Note that the value defined by an object definition is instantiated lazily. The new m$cls constructor is
        // evaluated not at the point of the object definition, but is instead evaluated the first time m is
        // dereferenced during execution of the program (which might be never at all). An attempt to dereference m
        // again in the course of evaluation of the constructor leads to a infinite loop or run-time error. Other
        // threads trying to dereferencem while the constructor is being evaluated block until evaluation is complete.

        // This seems to do the trick without referring to any internals.
        // classOf[ComputationGraph] does not compile, so the Java version is used.
        ComputationGraph.getClass
        // However, it is more important to start in a known state.
        ComputationGraph.renew()
        inSynchronized = false
      }
    }
  }
}
