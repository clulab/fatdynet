package org.clulab.fatdynet.test

import org.clulab.fatdynet.utils.Utils
import org.scalatest._

class TestUtils extends FlatSpec with Matchers {

  behavior of "utils"

  it should "detect an operating system" in {
    Utils.isKnownOS should be (true)
  }

  it should "be able to load the dynet library" in {
    Utils.loadDynet() should be (true)
  }
}

