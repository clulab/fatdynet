package org.clulab.fatdynet.utils

import edu.cmu.dynet._

// According to devices.cc, "Devices cannot be deleted at the moment because
// the destructor is protected."  Cleanup is therefore disallowed.
// At the very least, it causes which crashes the test suite.
object Initializer {
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
    Initialize.initialize(args)
    initialized = true
    oldInitialized
  }
}
