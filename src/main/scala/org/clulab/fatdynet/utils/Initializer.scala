package org.clulab.fatdynet.utils

import edu.cmu.dynet._

object Initializer {
  protected var initialized: Boolean = false

  protected def cleanup() {
    internal.dynet_swig.cleanup()
  }

  def isInitialized: Boolean = this.synchronized { initialized }

  // Returns whether had previously been initialized or not.
  def initialize(args: Map[String, Any] = Map.empty): Boolean = this.synchronized {
    val oldInitialized = initialized

    if (initialized)
      cleanup()
    initialized = true
    Initialize.initialize(args)
    oldInitialized
  }
}
