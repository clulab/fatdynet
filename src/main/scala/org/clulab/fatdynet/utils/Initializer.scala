package org.clulab.fatdynet.utils

import edu.cmu.dynet._
import edu.cmu.dynet.internal.dynet_swig.reset_rng

import java.util.concurrent.atomic.AtomicBoolean

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

  protected val initialized: AtomicBoolean = new AtomicBoolean(false)

  protected def rawCleanup(): Boolean = {
    val oldInitialized = initialized.get

    if (oldInitialized) {
      // internal.dynet_swig.cleanup()
      // initialized = false
    }
    oldInitialized
  }

  def cleanup(): Boolean = Synchronizer.withoutComputationGraph("Initializer.cleanup") {
    rawCleanup()
  }

  def isInitialized: Boolean = initialized.get

  // Returns whether had previously been initialized or not.
  def initialize(args: Map[String, Any] = Map.empty): Boolean = Synchronizer.withoutComputationGraph("Initializer.initialize") {
    val oldInitialized = rawCleanup()

    if (oldInitialized && args.contains(RANDOM_SEED)) {
      println("It was already initialized")
      // The initialization would have been ignored,
      // so the random seed will be set explicitly.
      val seed = args(RANDOM_SEED).asInstanceOf[Long]

      reset_rng(seed)
      // Imitate normal initialization output.
      System.err.println(s"[dynet] random seed: $seed")
    }
    // This is still required, even if it was previously initialized.  Why?
    Initialize.initialize(args)
    initialized.set(true)
    oldInitialized
  }
}
