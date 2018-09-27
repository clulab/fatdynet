package org.clulab.fatdynet

import org.scalatest._

class TestEnv extends FlatSpec with Matchers {

  behavior of "sbt"

  it should "see the have set LD_LIBRARY_PATH correctly for Linux" in {
    System.getenv("LD_LIBRARY_PATH") should not be (null)
  }

  it should "have set DYLD_LIBRARY_PATH correctly for Mac" in {
    System.getenv("DYLD_LIBRARY_PATH") should not be (null)
  }
}

