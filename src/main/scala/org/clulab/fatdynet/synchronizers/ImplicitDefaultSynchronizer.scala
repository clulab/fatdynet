package org.clulab.fatdynet.synchronizers

import edu.cmu.dynet.ComputationGraph

// This is the traditional implementation.  The global ComputationGraph is implicit.
// Synchronization is also global and provided by Synchronizer.synchronized.
class DebugImplicitDefaultSynchronizer(override val verbose: Boolean) extends DebugSynchronizer with ImplicitSynchronizer with Synchronizer {

  def withComputationGraph[T](message: Any)(f: => T): T = {
    // In parallel version, synchronize on Thread.currentThread or ComputationGraph.
    Synchronizer.synchronized {
      enter()
      try {
        ComputationGraph.renew()

        val startVersionOpt = Some(ComputationGraph.version)
        val index = before(message, startVersionOpt)

        try {
          f
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

class ReleaseImplicitDefaultSynchronizer extends ReleaseSynchronizer with ImplicitSynchronizer with Synchronizer {

  def withComputationGraph[T](message: Any)(f: => T): T = {
    Synchronizer.synchronized {
      enter()
      try {
        ComputationGraph.renew()
        f
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
        // The ComputationGraph is not touched here.
        f
      }
      finally {
        exit()
      }
    }
  }
}
