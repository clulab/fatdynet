package org.clulab.fatdynet.utils

//import edu.cmu.dynet.internal
import edu.cmu.dynet.ComputationGraph
import org.clulab.fatdynet.utils.Closer.AutoCloser

import java.util.concurrent.atomic.AtomicBoolean

trait Synchronizer {
  protected val synchronizing = new AtomicBoolean(false)

  // Allow public query of the read-only version.
  def isSynchronized: Boolean = synchronizing.get

  def enter(): Unit = {
    if (synchronizing.getAndSet(true))
      throw Synchronizer.newSynchronizationException()
  }

  def exit(): Unit = {
    synchronizing.set(false)
  }

  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T
  def withoutComputationGraph[T](message: Any, newComputationGraph: Boolean)(f: => T): T
}

class DebugSynchronizer(verbose: Boolean) extends Synchronizer {
  protected var count = 0

  def log(index: Int, stage: String, startVersionOpt: Option[Long], message: Any): Unit = {
    val threadId: Long = Thread.currentThread.getId
    val version = startVersionOpt.map(_.toString).getOrElse("?")
    println(s"Synchronizer\tindex\tstage\tthreadId\tversion\tmessage")
    println(s"Synchronizer\t$index\t$stage\t$threadId\t$version\t${message.toString}")
  }

  def before(message: Any, startVersionOpt: Option[Long]): Int = {
    try {
      val index = count
      if (verbose) log(index, "before", startVersionOpt, message)
      count += 1 // Something else will see a different count now.
      index
    }
    catch {
      case throwable: Throwable =>
        if (verbose) throwable.printStackTrace()
        throw throwable
    }
  }

  def during[T](f: => T): T = {
    try {
      val result = f // This needs to make all the nodes
      result
    }
    catch {
      case throwable: Throwable =>
        if (verbose) throwable.printStackTrace()
        throw throwable
    }
  }

  def after(message: Any, index: Int, newComputationGraph: Boolean, startVersionOpt: Option[Long], endVersionOpt: Option[Long]): Unit = {
    try {
      if (verbose) log(index, "after", startVersionOpt, message)
      require(startVersionOpt == endVersionOpt, "ComputationGraph version should not change")

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
      // ComputationGraph.getClass
      // However, it is more important to start in a known state.
      if (newComputationGraph: Boolean)
        ComputationGraph.renew()
    }
    catch {
      case throwable: Throwable =>
        if (verbose) throwable.printStackTrace()
        throw throwable
    }
  }

  def doSynchronized[T](message: Any, newComputationGraph: Boolean, f: ComputationGraph => T, getVersionOpt: () => Option[Long]): T = {
    val startVersion = getVersionOpt()
    val index = before(message, startVersion)

    try {
      ComputationGraph.renew(true).autoClose { cg =>
        f(cg)
      }
    }
    finally {
      val endVersion = getVersionOpt()
      after(message, index, newComputationGraph, startVersion, endVersion)
    }
  }

  def doSynchronized[T](message: Any, newComputationGraph: Boolean, f: => T, getVersionOpt: () => Option[Long]): T = {
    val startVersion = getVersionOpt()
    val index = before(message, startVersion)

    try {
      f
    }
    finally {
      val endVersion = getVersionOpt()
      after(message, index, newComputationGraph, startVersion, endVersion)
    }
  }

  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T = {
    // In parallel version, synchronize on Thread.currentThread or ComputationGraph.
    Synchronizer.synchronized {
      enter()
      try {
        doSynchronized(message, true, f, () => Some(ComputationGraph.version))
      }
      finally {
        exit()
      }
    }
  }

  def withoutComputationGraph[T](message: Any, newComputationGraph: Boolean)(f: => T): T = {
    // Synchronization here should be global.  There should be no active ComputationGraphs.
    Synchronizer.synchronized {
      enter()
      try {
        doSynchronized(message, newComputationGraph, f, () => None)
      }
      finally {
        exit()
      }
    }
  }
}

class ReleaseSynchronizer extends Synchronizer {

  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T = {
    Synchronizer.synchronized {
      enter()
      try {
        ComputationGraph.renew(true).autoClose { cg =>
          f(cg)
        }
      }
      finally {
        exit()
      }
    }
  }

  def withoutComputationGraph[T](message: Any, newComputationGraph: Boolean)(f: => T): T = {
    Synchronizer.synchronized {
      enter()
      try {
        f
      }
      finally {
        exit()
      }
    }
  }
}

object Synchronizer extends Synchronizer {
  var debug: Boolean = true
  var verbose: Boolean = false
  val synchronizer: Synchronizer =
      if (debug) new DebugSynchronizer(verbose)
      else new ReleaseSynchronizer()

  def newSynchronizationException(): SynchronizationException =
      new SynchronizationException("FatDynet is already being synchronized.")

  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T = synchronizer.withComputationGraph(message)(f)

  def withoutComputationGraph[T](message: Any, newComputationGraph: Boolean = false)(f: => T): T = synchronizer.withoutComputationGraph(message, newComputationGraph)(f)
}
