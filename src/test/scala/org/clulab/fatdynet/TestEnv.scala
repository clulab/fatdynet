package org.clulab.fatdynet

import org.scalatest._

class TestEnv extends FlatSpec with Matchers {

  behavior of "sbt"

  it should "have set LD_LIBRARY_PATH correctly for Linux" in {
    assert(System.getenv("LD_LIBRARY_PATH") == ".")
  }

  it should "have set DYLD_LIBRARY_PATH correctly for Mac" in {
    assert(System.getenv("DYLD_LIBRARY_PATH") == ".")
  }
}

