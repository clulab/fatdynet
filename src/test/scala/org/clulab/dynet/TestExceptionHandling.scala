package org.clulab.dynet

import edu.cmu.dynet.internal.{dynet_swig => dynet}
import org.clulab.fatdynet.utils.Initializer
import org.scalatest._

class TestExceptionHandling extends FlatSpec with Matchers {
  val isWindows: Boolean = System.getProperty("os.name").toLowerCase().contains("win")

  def testException(thrower: => Unit, name: String, method: String, text: String): Unit = {
    val thrown = the [RuntimeException] thrownBy {
      thrower
    }
    val message = thrown.getMessage
    println(message)

//    message should include (name)
//    message should include (method)
    message should include (text)
  }

  Initializer.initialize()

  behavior of "exception handling routines"

  it should "handle a runtime error" in {
    testException(dynet.throwRuntimeError(),
      "std::runtime_error",
      "dynet::throwRuntimeError",
      "This is a runtime error." // determined by dynet
    )
  }

  it should "handle a subclass of runtime error" in {
    testException(dynet.throwSubRuntimeError(),
      "std::runtime_error",
      "dynet::throwSubRuntimeError",
      "This is an overflow error, a kind of runtime error" // determined by dynet
    )
  }

  it should "handle a logic error" in {
    testException(dynet.throwLogicError(),
      "std::logic_error",
      "dynet::throwLogicError",
      "This is a logic error" // determined by dynet
    )
  }

  it should "handle a subclass of logic error" in {
    testException(dynet.throwSubLogicError(),
      "std::logic_error",
      "dynet::throwSubLogicError",
      "This is a domain error, a kind of logic error." // determined by dynet
    )
  }

  it should "handle an exception" in {
    testException(dynet.throwException(),
      "std::exception",
      "dynet::throwException",
      "Unknown exception"
    )
  }

  it should "handle a subclass of exception" in {
    testException(dynet.throwSubException(),
      "std::exception",
      "dynet::throwSubException",
      if (isWindows) "bad cast" else "bad_cast" // determined by C++
    )
  }

  it should "handle an unknown exception" in {
    testException(dynet.throwUnknown(),
      "...",
      "dynet::throwUnknown",
      "unknown exception" // determined by swig
    )
  }
}
