package org.clulab.fatdynet.utils

import edu.cmu.dynet._
import edu.cmu.dynet.internal.dynet_swig.reset_rng

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

  def cleanup(): Boolean = this.synchronized {
    val oldInitialized = initialized

    if (oldInitialized) {
//      internal.dynet_swig.cleanup()
//      initialized = false
    }
    oldInitialized
  }

  def isInitialized: Boolean = this.synchronized { initialized }

  // Returns whether had previously been initialized or not.
  def initialize(args: Map[String, Any] = Map.empty): Boolean = this.synchronized {
    val oldInitialized = initialized

    cleanup()
    if (oldInitialized && args.contains(RANDOM_SEED)) {
      // The initialization would have been ignored,
      // so the random seed will be set explicitly.
      val seed = args(RANDOM_SEED).asInstanceOf[Long]

      reset_rng(seed)
      // Imitate normal initialization output.
      System.err.println(s"[dynet] random seed: $seed")
    }
    Initialize.initialize(args)
    ComputationGraph.getClass
    initialized = true
    oldInitialized
  }
}
