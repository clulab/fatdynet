package org.clulab.fatdynet.synchronizers

class ExplicitCustomSynchronizer {

}
/*
class DebugExplicitDefaultSynchronizer(override val verbose: Boolean) extends DebugSynchronizer with ExplicitSynchronizer with Synchronizer {

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
*/