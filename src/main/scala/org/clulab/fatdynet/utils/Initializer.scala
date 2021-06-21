package org.clulab.fatdynet.utils

import edu.cmu.dynet._
import edu.cmu.dynet.internal.dynet_swig.reset_rng

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

// According to devices.cc, "Devices cannot be deleted at the moment because
// the destructor is protected."  Cleanup is therefore disallowed.
// At the very least, it causes which crashes the test suite.
object Initializer {
  // See Initializer.scala for keys.
  val DYNET_MEM = "dynet-mem"
  val RANDOM_SEED = "random-seed"
  val WEIGHT_DECAY = "weight-decay"
  val SHARED_PARAMETERS = "shared-parameters"
  val AUTOBATCH = "autobatch"
  val PROFILING = "profiling"
  val DYNAMIC_MEM = "dynamic-mem"
  val FORWARD_ONLY = "forward-only"

  protected var initialized: Boolean = false

  def isInitialized: Boolean = initialized

  def isThreaded: Boolean = Synchronizer.threaded

  // In this special case we do not want a new ComputationGraph at the end.
  def cleanup(): Boolean = Initializer.synchronized {
    val oldInitialized = initialized

    if (oldInitialized) {
      Synchronizer.withoutComputationGraph("Initializer.cleanup", false) {
        internal.dynet_swig.cleanup()
      }
      initialized = false
    }
    oldInitialized
  }

  protected def isThreaded(args: Map[String, Any]): Boolean = {
    lazy val dynamicMem = args.get(DYNAMIC_MEM).map(_.asInstanceOf[Boolean]).getOrElse(false)
    lazy val forwardOnly = args.get(FORWARD_ONLY).map(_.asInstanceOf[Int]).getOrElse(0) != 0

    dynamicMem && forwardOnly
  }

  // Returns whether had previously been initialized or not.
  def initialize(args: Map[String, Any] = Map.empty): Boolean = Initializer.synchronized {
    val oldInitialized = initialized

    if (!oldInitialized) {
      Synchronizer.threaded = isThreaded(args)
      Synchronizer.withoutComputationGraph("Initializer.initialize") {
        Initialize.initialize(args)
      }
      initialized = true
    }
    else if (args.contains(RANDOM_SEED)) {
      // The initialization would have been ignored,
      // so the random seed will be set explicitly.
      val seed = args(RANDOM_SEED).asInstanceOf[Long]

      Synchronizer.withoutComputationGraph("Initializer.initialize") {
        reset_rng(seed)
      }
      // Imitate normal initialization output.
      System.err.println(s"[dynet] random seed: $seed")
    }
    oldInitialized
  }
}
