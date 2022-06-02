package org.clulab.fatdynet.synchronizers

import edu.cmu.dynet.ComputationGraph
import org.clulab.fatdynet.utils.SynchronizationException

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

// Synchronization prevents two different threads from entering the critical section
// at the same time.  It does not prevent the same thread from entering a critical
// section twice.  This happens when there is unintentional recursion, for example.
// The enter and exit methods are supposed to detect that situation.
trait Synchronizer {
  protected val synchronizing = new AtomicBoolean(false)

  // Allow public query of the read-only version.
  def isSynchronized: Boolean = synchronizing.get

  // Should this be a function with a try and finally instead?
  def enter(): Unit = {
    if (synchronizing.getAndSet(true))
      throw Synchronizer.newSynchronizationException()
  }

  def exit(): Unit = {
    synchronizing.set(false)
  }
}

trait DebugSynchronizer {
  protected var count = new AtomicInteger(0)
  protected val verbose: Boolean = false

  def log(index: Int, stage: String, startVersionOpt: Option[Long], cgOpt: Option[ComputationGraph], message: Any): Unit = {
    val threadId: Long = Thread.currentThread.getId
    val version = startVersionOpt.map(_.toString).getOrElse("?")
//    println(s"Synchronizer\tindex\tstage\tthreadId\tversion\tcgOpt\tmessage")
    println(s"Synchronizer\t$index\t$stage\t$threadId\t$version\t$cgOpt\t${message.toString}")
  }

  def before(message: Any, startVersionOpt: Option[Long], cgOpt: Option[ComputationGraph]): Int = {
    try {
      val index = count.getAndIncrement()
      if (verbose) log(index, "before", startVersionOpt, cgOpt, message)
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

  def after(message: Any, index: Int, startVersionOpt: Option[Long], endVersionOpt: Option[Long]): Unit = {
    try {
      if (verbose) log(index, "after", startVersionOpt, None, message)
      if (startVersionOpt != endVersionOpt)
        println("Oh, no!")
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
    }
    catch {
      case throwable: Throwable =>
        if (verbose) throwable.printStackTrace()
        throw throwable
    }
  }
}

trait ReleaseSynchronizer

// In the implicit version, the f does not use a ComputationGraph.
// It will be taken from the globally defined value by things that need it.
trait ImplicitSynchronizer {
  def withComputationGraph[T](message: Any)(f: => T): T
  def withoutComputationGraph[T](message: Any)(f: => T): T
}

// In the explicit version, the f does indeed take a ComputationGraph.
// That cg can be inserted into the context as an implicit value or be
// passed around explitly as an argument.  Adding it to the context takes
// care of some compatibility issues in transitioning from one to the other.
trait ExplicitSynchronizer {
  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T
  def withoutComputationGraph[T](message: Any)(f: => T): T
}

// For the internal, Java representation
// reset(boolean ignoreSingleton) is new
// getNew(boolean ignoreStatis) is new

// Should initializer decide that kind of Synchronizer is being used?
// Make some way to initialize this?  If necessary, via the initializer?
object Synchronizer {
  var debug: Boolean = true
  var verbose: Boolean = false
  var ignoreStatic: Boolean = true
  var single: Boolean = true

  def canTrain: Boolean = single && !ignoreStatic

  def newSynchronizer(debug: Boolean, verbose: Boolean, ignoreStatic: Boolean, single: Boolean): ExplicitSynchronizer =
      if (single)
        if (debug) new DebugExplicitSingleSynchronizer(ignoreStatic, verbose)
        else new ReleaseExplicitSingleSynchronizer(ignoreStatic)
      else
        if (debug) new DebugExplicitMultipleSynchronizer(verbose)
        else new ReleaseExplicitMultipleSynchronizer()

  var synchronizer = newSynchronizer(debug, verbose, ignoreStatic, single)

  def update(): Unit = synchronizer = newSynchronizer(debug, verbose, ignoreStatic, single)

  def newSynchronizationException(): SynchronizationException =
      new SynchronizationException("FatDynet is already being synchronized.")

//  def withComputationGraph[T](message: Any)(f: => T): T = synchronizer.withComputationGraph(message)(f)

  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T = synchronizer.withComputationGraph(message)(f)

  def withoutComputationGraph[T](message: Any)(f: => T): T = synchronizer.withoutComputationGraph(message)(f)
}
