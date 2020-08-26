package org.clulab.dynet

import edu.cmu.dynet.internal.{ComputationGraph => JavaComputationGraph}
import edu.cmu.dynet.{ComputationGraph => ScalaComputationGraph}
import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestComputationGraph extends FlatSpec with Matchers {
  Xor.initialize()

  val xorParameters = new Xor.XorParameters()

  behavior of "Java ComputationGraph"

  it should "support getNew()" in {
    val cg1 = JavaComputationGraph.getNew
    cg1.clear()

    val cg2 = JavaComputationGraph.getNew
    cg2.clear()

    cg1.eq(cg2) should be (false)

    // This should no longer work.  It will completely crash Java.
    // cg1.clear()
  }

  behavior of "Scala ComputationGraph"

  it should "support renew()" in {
    ScalaComputationGraph.version should be (0)
    ScalaComputationGraph.renew()
    ScalaComputationGraph.version should be (1)
    ScalaComputationGraph.renew()
    ScalaComputationGraph.version should be (2)
  }

  behavior of "defaultXor"

  it should "run" in {
    val loss = Xor.defaultXor(xorParameters)

    loss should be (Xor.expectedLoss)
  }
}
