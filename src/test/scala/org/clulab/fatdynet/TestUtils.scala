package org.clulab.fatdynet

import org.scalatest._

class TestUtils extends FlatSpec with Matchers {

  behavior of "utils"

  it should "detect an operating system" in {
    Utils.isKnownOS should be (true)
  }

  it should "find the dynet library file" in {
    Utils.isDynetFileAvailable should be (true)
  }

  it should "be able to load the dynet library" in {
    Utils.isDynetLibAvailable should be (true)
  }

}

