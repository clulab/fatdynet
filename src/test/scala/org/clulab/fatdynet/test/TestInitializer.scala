package org.clulab.fatdynet.test

import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.utils.Initializer

// Some of these tests are disabled because cleanup is disabled.
class TestInitializer extends FatdynetTest {

  behavior of "Initializer"

  it should "start out uninitialized" in {
//    Initializer.cleanup()
//    Initializer.isInitialized should be (false)
  }

  it should "initialize and indicate previously uninitialized" in {
//    Initializer.initialize() should be (false)
    Initializer.isInitialized should be (true)
  }

  it should "reinitialize and indicate previously initialized" in {
    Initializer.initialize() should be (true)
    Initializer.isInitialized should be (true)
  }
}
