package org.clulab.dynet.solo

import edu.cmu.dynet.internal.{ComputationGraph => JavaComputationGraph}
import edu.cmu.dynet.{ComputationGraph => ScalaComputationGraph}
import org.clulab.fatdynet.Test
import org.clulab.fatdynet.utils.Initializer

class TestComputationGraph extends Test {
  Initializer.initialize()

  behavior of "Java ComputationGraph"

  it should "support getNew()" in {
    val cg1 = JavaComputationGraph.getNew
    cg1.clear()
    cg1.delete()

    val cg2 = JavaComputationGraph.getNew
    cg2.clear()
    cg2.delete()

    cg1.eq(cg2) should be (false)
    // This must be deleted if the new ScalaComputationGraph is used
    // which doesn't rely on the JavaComputationGraph singleton.
    cg2.delete()

    // This should no longer work.  It will completely crash Java.
    // cg1.clear()
  }

  behavior of "Scala ComputationGraph"

  it should "support renew()" in {
    val version = ScalaComputationGraph.version

    ScalaComputationGraph.version should be (version)
    ScalaComputationGraph.renew()
    ScalaComputationGraph.version should be (version + 1)
    ScalaComputationGraph.renew()
    ScalaComputationGraph.version should be (version + 2)
  }
}
