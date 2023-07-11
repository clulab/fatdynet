package org.clulab.fatdynet.apps

import org.clulab.fatdynet.utils.Initializer

object TestInitializerApp extends App {
  // We do not want to call cleanup in a test suite.
  // Nothing else should be run after it.

  {
    Initializer.cleanup()
    assert(!Initializer.isInitialized)
  }

  {
    Initializer.cleanup()
    assert(!Initializer.initialize())
    assert(Initializer.isInitialized)
  }

  {
    Initializer.cleanup()
    Initializer.initialize()
    assert(Initializer.initialize())
    assert(Initializer.isInitialized)
  }
}
