package org.clulab.dynet

import edu.cmu.dynet.internal.{dynet_swig => dynet}
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.utils.Initializer

class TestSignalHandling extends FatdynetTest {
  val SIGSEGV = TestSignalHandling.SIGSEGV
  val isWindows: Boolean = System.getProperty("os.name").toLowerCase().contains("win")

  Initializer.cluInitialize()

  behavior of "signal handling routines"

  it should "do something" in {
    // This activates beforeAll and afterAll.
  }

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
