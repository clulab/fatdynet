package org.clulab.fatdynet.synchronizers

import edu.cmu.dynet.ComputationGraph
import org.clulab.fatdynet.utils.Closer.AutoCloser

// This is the same as the traditional implementation except that the global ComputationGraph
// has been made explicit and is forwarded as an argument to a function provided.
// Because there is still only one ComputationGraph being used, synchronization is also global
// and provided by Synchronizer.synchronized.
class DebugExplicitDefaultSynchronizer(override val verbose: Boolean) extends DebugSynchronizer with ExplicitSynchronizer with Synchronizer {
  val ignoreStatic = false

  def doSynchronized[T](message: Any, newComputationGraph: Boolean, f: ComputationGraph => T, getVersionOpt: () => Option[Long]): T = {
    // This renew() may change the version.
    val cg = ComputationGraph.renew(ignoreStatic)
    val startVersion = getVersionOpt()
    val index = before(message, newComputationGraph, startVersion)

    try {
      cg.autoClose { cg =>
        f(cg)
      }
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

class ReleaseExplicitDefaultSynchronizer extends ReleaseSynchronizer with ExplicitSynchronizer with Synchronizer {
  val ignoreStatic = false

  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T = {
    Synchronizer.synchronized {
      enter()
      try {
        ComputationGraph.renew(ignoreStatic).autoClose { cg =>
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
