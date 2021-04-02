package org.clulab.fatdynet

import org.clulab.fatdynet.utils.Utils
import org.scalatest.BeforeAndAfterAll

class FatdynetTest extends Test with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    Utils.startup()
  }

  override def afterAll(): Unit = {
    Utils.shutdown()
  }
}
