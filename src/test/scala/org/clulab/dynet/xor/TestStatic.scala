package org.clulab.dynet.xor

import edu.cmu.dynet.ComputationGraph

class TestStaticComputationGraph extends TestXor {
  // This results in the single ComputationGraph being used.
  Xor.initialize(train = true)

  val xorParameters = new Xor.XorParameters()

  def f(parallel: Boolean): Float = {
    if (parallel) {
      Xor.synchronized {
        // if there was previously a graph, it needs to be reset.
        ComputationGraph.reset()
        Xor.runDefault(xorParameters)
      }
    }
    else
      Xor.runDefault(xorParameters)
  }

  test("static Xor", f)
}
