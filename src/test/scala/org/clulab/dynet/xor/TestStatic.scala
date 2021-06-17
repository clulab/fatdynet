package org.clulab.dynet.xor

import edu.cmu.dynet.ComputationGraph

class TestStaticComputationGraph extends TestXor {
  // This results in the single ComputationGraph being used.
  Xor.initialize(train = true)

  val xorParameters = new Xor.XorParameters()

  def f(parallel: Boolean): Float = {
    if (parallel) {
      this.synchronized {
        val result = Xor.runDefault(xorParameters)
        // Get rid of the graph used in this thread, because the one reset just before
        // the next loop might be in a different thread and leave this one dangling.
        ComputationGraph.reset()
        result
      }
    }
    else
      Xor.runDefault(xorParameters)
  }

  test("static Xor", f)
}
