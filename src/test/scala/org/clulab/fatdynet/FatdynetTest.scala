package org.clulab.fatdynet

import org.clulab.fatdynet.utils.Utils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.scalatest.Matchers

class FatdynetTest extends FlatSpec with Matchers with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    Utils.startup()
  }

  override def afterAll(): Unit = {
    Utils.shutdown()
  }
}
