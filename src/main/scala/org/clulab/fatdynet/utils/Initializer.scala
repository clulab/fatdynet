package org.clulab.fatdynet.utils

import edu.cmu.dynet._

object Initializer {
  protected var initialized: Boolean = false

  // Returns whether had already been initialized and doesn't do it again.
  def initialize(args: Map[String, Any] = Map.empty): Boolean = synchronized {
    if (initialized) {
      println("Keith is ignoring duplicate initialization.")
      true
    }
    else {
      Initialize.initialize(args)
      initialized = true
      false
    }
  }
}
