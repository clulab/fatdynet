package org.clulab.fatdynet.synchronizers

import edu.cmu.dynet.ComputationGraph
import org.clulab.fatdynet.utils.Closer.AutoCloser

// This is the same as the traditional implementation except that the global ComputationGraph
// has been made explicit and is forwarded as an argument to a function provided.
// Because there is still only one ComputationGraph being used, synchronization is also global
// and provided by Synchronizer.synchronized.
class DebugExplicitSingleSynchronizer(ignoreStatic: Boolean, override val verbose: Boolean) extends DebugSynchronizer with ExplicitSynchronizer with Synchronizer {

  def withComputationGraph[T](message: Any)(f: ComputationGraph => T): T = {
    Synchronizer.synchronized {
      enter()
      try {
        val cg = ComputationGraph.renew(ignoreStatic)
        val startVersionOpt = Some(ComputationGraph.version)
        val index = before(message, startVersionOpt, Some(cg))

        try {
          f(cg)
        }
        finally {
          val endVersion = Some(ComputationGraph.version)

          after(message, index, startVersionOpt, endVersion)
        }
      }
      finally {
        exit()
      }
    }
  }

  def withoutComputationGraph[T](message: Any)(f: => T): T = {
    // Synchronization here should be global.  There should be no active ComputationGraphs.
    Synchronizer.synchronized {
      enter()
      try {
        val startVersionOpt = None
        val index = before(message, startVersionOpt, None)

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

class ReleaseExplicitSingleSynchronizer(ignoreStatic: Boolean) extends ReleaseSynchronizer with ExplicitSynchronizer with Synchronizer {

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
