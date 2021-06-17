package org.clulab.dynet.xor

class TestDynamic extends TestXor {
  // This results in multiple ComputationGraphs being available, one per thread.
  Xor.initialize(false)

  val xorParameters = new Xor.XorParameters()

  // Nothing else
  def f(parallel: Boolean): Float = Xor.runDefault(xorParameters)

  test("dynamic Xor", f)
}
