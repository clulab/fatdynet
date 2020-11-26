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
  val SIGSEGV = TestSignalHandling.SIGSEGV
  val isWindows: Boolean = System.getProperty("os.name").toLowerCase().contains("win")

  Initializer.initialize()

  behavior of "signal handling routines"

  it should "work using the C++ signal handler" in {
    val signalHandler = new SignalHandler()
    dynet.setSignalHandler(SIGSEGV, signalHandler)
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
    dynet.resetSignalHandler(SIGSEGV)
  }

  it should "work using the Java signal handler" in {
    val signalHandler = new JavaSignalHandler()
    dynet.setSignalHandler(SIGSEGV, signalHandler)
    signalHandler.count should be (0)
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
    signalHandler.count should be (1)
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
    signalHandler.count should be (2)
    dynet.resetSignalHandler(SIGSEGV)
  }

  it should "work using the Scala signal handler" in {
    val signalHandler = new ScalaSignalHandler()
    dynet.setSignalHandler(SIGSEGV, signalHandler)
    signalHandler.count should be (0)
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
    signalHandler.count should be (1)
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
    signalHandler.count should be (2)
    dynet.resetSignalHandler(SIGSEGV)
  }

  it should "handle a null pointer read" in {
    val signalHandler = new ScalaSignalHandler()
    dynet.setSignalHandler(SIGSEGV, signalHandler)
    signalHandler.count should be (0)
    the [RuntimeException] thrownBy {
      dynet.readNullPtr()
    }
    signalHandler.count should be (1)
    the [RuntimeException] thrownBy {
      dynet.readNullPtr()
    }
    signalHandler.count should be (2)
    dynet.resetSignalHandler(SIGSEGV)
  }

  it should "handle a null pointer write" in {
    val signalHandler = new ScalaSignalHandler()
    dynet.setSignalHandler(SIGSEGV, signalHandler)
    signalHandler.count should be (0)
    the [RuntimeException] thrownBy {
      dynet.writeNullPtr()
    }
    signalHandler.count should be (1)
    the [RuntimeException] thrownBy {
      dynet.writeNullPtr()
    }
    signalHandler.count should be (2)
    dynet.resetSignalHandler(SIGSEGV)
  }
}

object TestSignalHandling {
  val SIGSEGV = 11
}
