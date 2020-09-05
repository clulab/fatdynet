package org.clulab.dynet

import edu.cmu.dynet.internal.{ComputationGraph => JavaComputationGraph}
import edu.cmu.dynet.{ComputationGraph => ScalaComputationGraph}
import org.clulab.fatdynet.utils.Initializer

class TestComputationGraph extends Test {
  Initializer.initialize(Map(Initializer.DYNAMIC_MEM -> false))

  // Act like there is a computation graph from different Scala tests.  The Scala
  // version does not clue in the Java version of the one computation graph limitation
  // under non-dynamic circumstances.  The Java singletonInstance will not be assigned
  // so that nothing knows to delete the existing instance.  Leave it unassigned but
  // then manually delete the existing instance so that Java is in sync.
  val version = ScalaComputationGraph.version
  val javaComputationGraph =
    try {
      JavaComputationGraph.getNew
    }
    catch {
      case throwable: Throwable => ScalaComputationGraph.reset()
    }

  behavior of "Java ComputationGraph"

  it should "support getNew()" in {
    val cg1 = JavaComputationGraph.getNew
    cg1.clear()

    val cg2 = JavaComputationGraph.getNew
    cg2.clear()

    cg1.eq(cg2) should be (false)
    // This must be reset if the new ScalaComputationGraph is used
    // which doesn't rely on the JavaComputationGraph singleton.
    cg2.reset()

    // This should no longer work.  It will completely crash Java.
    // cg1.clear()
  }

  behavior of "Scala ComputationGraph"

  it should "support renew()" in {
    // This will assure the deleted one has been replaced.
    ScalaComputationGraph.renew()
    ScalaComputationGraph.version should be (0)
    ScalaComputationGraph.renew()
    ScalaComputationGraph.version should be (1)
    ScalaComputationGraph.renew()
    ScalaComputationGraph.version should be (2)
  }
}
