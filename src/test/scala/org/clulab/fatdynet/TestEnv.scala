package org.clulab.fatdynet

import org.scalatest._

class TestEnv extends FlatSpec with Matchers {

  behavior of "sbt"

  it should "have set LD_LIBRARY_PATH correctly" in {
    assert(System.getenv("LD_LIBRARY_PATH") == ".")
  }
}

