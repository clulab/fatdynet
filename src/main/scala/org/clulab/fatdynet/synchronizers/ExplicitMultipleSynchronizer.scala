package org.clulab.fatdynet.synchronizers

import edu.cmu.dynet.ComputationGraph
import org.clulab.fatdynet.utils.Closer.AutoCloser

// This is the same as the traditional implementation except that the global ComputationGraph
// has been made explicit and is forwarded as an argument to a function provided.
// Because there is still only one ComputationGraph being used, synchronization is also global
// and provided by Synchronizer.synchronized.
class DebugExplicitMultipleSynchronizer(override val verbose: Boolean) extends DebugSynchronizer with ExplicitSynchronizer with Synchronizer {
  val ignoreStatic = true

  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T = {
    ComputationGraph.renew(true).autoClose { cg =>
      val startVersionOpt = None
      val index = before(message, startVersionOpt)

      try {
        f(cg)
      }
      finally {
        val endVersionOpt = None

        after(message, index, startVersionOpt, endVersionOpt)
      }
    }
  }

  def withoutComputationGraph[T](message: Any)(f: => T): T = {
    // Synchronization here should be global.  There should be no active ComputationGraphs.
    Synchronizer.synchronized {
      enter()
      try {
        val startVersionOpt = None
        val index = before(message, startVersionOpt)

        try {
          f
        }
        finally {
          val endVersionOpt = None

          after(message, index, startVersionOpt, endVersionOpt)
        }
      }
      finally {
        exit()
      }
    }
  }
}

class ReleaseExplicitMultipleSynchronizer() extends ReleaseSynchronizer with ExplicitSynchronizer with Synchronizer {
  val ignoreStatic = true

  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T = {
    ComputationGraph.renew(true).autoClose { cg =>
      f(cg)
    }
  }

  def withoutComputationGraph[T](message: Any)(f: => T): T = {
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
