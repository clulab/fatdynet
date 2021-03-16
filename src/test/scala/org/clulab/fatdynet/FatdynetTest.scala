package org.clulab.fatdynet

import org.clulab.fatdynet.utils.Utils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.{AnyFlatSpec => FlatSpec}
import org.scalatest.matchers.should.{Matchers => Matchers}

class FatdynetTest extends FlatSpec with Matchers with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    Utils.startup()
  }

  override def afterAll(): Unit = {
    Utils.shutdown()
  }
}
