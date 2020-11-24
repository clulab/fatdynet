package org.clulab.dynet

import edu.cmu.dynet.internal.Callback
import edu.cmu.dynet.internal.Caller
import org.clulab.fatdynet.utils.Initializer
import org.scalatest._

class ScalaCallback extends Callback {
  var count = 0

  override def run(): Unit = {
    count += 1
    println("ScalaCallback.run()")
  }
}

class TestCallback extends FlatSpec with Matchers {

  Initializer.initialize()

  behavior of "exception handling routines"

  it should "work using the C++ callback" in {
    val caller = new Caller()
    val callback = new Callback()

    caller.setCallback(callback)
    caller.call()
    caller.delCallback()
  }

  it should "work using the Java callback" in {
    val caller = new Caller()
    val callback = new JavaCallback()

    callback.count should be (0)
    caller.setCallback(callback)
    caller.call()
    callback.count should be (1)
    caller.delCallback()
  }

  it should "work using the Scala callback" in {
    val caller = new Caller()
    val callback = new ScalaCallback()

    callback.count should be (0)
    caller.setCallback(callback)
    caller.call()
    callback.count should be (1)
    caller.delCallback()
  }
}
