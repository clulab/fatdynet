package org.clulab.fatdynet.utils

import edu.cmu.dynet._

object Initializer {
  protected var initialized: Boolean = false
  protected var mostRecentArgs: Option[Map[String, Any]] = None

  // Returns whether had already been initialized and doesn't do it again.
  def initialize(args: Map[String, Any] = Map.empty): Boolean = synchronized {
    println(s"New arguments: $args")
    if (initialized) {
      println("Keith is ignoring duplicate initialization.")
      if (mostRecentArgs.get != args)
        println("New arguments do not match current arguments!")
      true
    }
    else {
      Initialize.initialize(args)
      initialized = true
      mostRecentArgs = Some(args)
      false
    }
  }
}
