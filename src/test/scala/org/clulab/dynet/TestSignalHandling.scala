package org.clulab.dynet

import edu.cmu.dynet.internal.{dynet_swig => dynet}
import org.clulab.fatdynet.utils.Initializer
import org.scalatest._

class TestSignalHandling extends FlatSpec with Matchers {
  val SIGSEGV = TestSignalHandling.SIGSEGV
  val isWindows: Boolean = System.getProperty("os.name").toLowerCase().contains("win")

  Initializer.initialize()

  behavior of "signal handling routines"

  ignore should "work using the C++ signal handler" in {
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
  }

  ignore should "work using the Java signal handler" in {
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
    the [RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
  }

  ignore should "work using the Scala signal handler" in {
    the[RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
    the[RuntimeException] thrownBy {
      dynet.raiseSignal(SIGSEGV)
    }
  }

  ignore should "handle a null pointer read" in {
    the [RuntimeException] thrownBy {
      dynet.readNullPtr()
    }
    the [RuntimeException] thrownBy {
      dynet.readNullPtr()
    }
  }

  ignore should "handle a null pointer write" in {
    the [RuntimeException] thrownBy {
      dynet.writeNullPtr()
    }
    the [RuntimeException] thrownBy {
      dynet.writeNullPtr()
    }
  }
}

object TestSignalHandling {
  val SIGSEGV = 11
}
