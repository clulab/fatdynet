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

class ReleaseImplicitDefaultSynchronizer extends ReleaseSynchronizer with ImplicitSynchronizer with Synchronizer {

  def withComputationGraph[T](message: Any)(f: => T): T = {
    Synchronizer.synchronized {
      enter()
      try {
        f
      }
      finally {
        try {
          ComputationGraph.renew()
        }
        finally {
          exit()
        }
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
        try {
          if (newComputationGraph)
            ComputationGraph.renew()
        }
        finally {
          exit()
        }
      }
    }
  }
}
