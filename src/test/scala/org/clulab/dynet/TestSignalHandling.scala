package org.clulab.dynet

import edu.cmu.dynet.internal.SignalHandler
import edu.cmu.dynet.internal.{dynet_swig => dynet}
import org.clulab.fatdynet.utils.Initializer
import org.scalatest._

class ScalaSignalHandler extends SignalHandler {
  var count = 0

  override def run(signal: Int): Int = {
    count += 1
    println("ScalaSignalHandler.run()")
    0
  }
}

class TestSignalHandling extends FlatSpec with Matchers {
  import TestSignalHandling.SIGSEGV
  var count = 0

  Initializer.initialize()

  behavior of "signal handling routines"

  it should "work using the C++ signal handler" in {
    val signalHandler = new SignalHandler()
    dynet.setSignalHandler(SIGSEGV, signalHandler)
    dynet.raiseSignal(SIGSEGV)
    dynet.resetSignalHandler(SIGSEGV)
  }

  it should "work using the Java signal handler" in {
    val signalHandler = new JavaSignalHandler()
    dynet.setSignalHandler(SIGSEGV, signalHandler)
    signalHandler.count should be (0)
    dynet.raiseSignal(SIGSEGV)
    signalHandler.count should be (1)
    dynet.resetSignalHandler(SIGSEGV)
  }

  it should "work using the Scala signal handler" in {
    val signalHandler = new ScalaSignalHandler()
    dynet.setSignalHandler(SIGSEGV, signalHandler)
    signalHandler.count should be (0)
    dynet.raiseSignal(SIGSEGV)
    signalHandler.count should be (1)
    dynet.resetSignalHandler(SIGSEGV)
  }

  ignore should "handle a null pointer read" in {
    // Get EXCEPTION_ACCESS_VIOLATION = 0xc0000005 on Windows
    try {
      dynet.readNullPtr()
    }
    catch {
      case throwable: Throwable =>
        throwable.printStackTrace()
    }
  }

  ignore should "handle a null pointer write" in {
    // Get EXCEPTION_ACCESS_VIOLATION = 0xc0000005 on Windows
    try {
      dynet.writeNullPtr()
    }
    catch {
      case throwable: Throwable =>
        throwable.printStackTrace()
    }
  }
}

object TestSignalHandling {
  val SIGSEGV = 11
}
