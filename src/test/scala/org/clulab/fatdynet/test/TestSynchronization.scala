package org.clulab.fatdynet.test

import org.clulab.fatdynet.Test

import java.util.concurrent.atomic.AtomicBoolean

class TestSynchronization extends Test {
  val count = 8

  behavior of "a synchronized section"

  it should "work in serial" in {
    0.until(count).foreach { index =>
      Synchronizer.synchronize()
    }
  }

  it should "work in parallel" in {
    0.until(count).par.foreach { index =>
      Synchronizer.synchronize()
    }
  }

  it should "catch a problem with lazy vals" in {
    assertThrows[SynchronizationException] {
      Synchronizer.synchronize(LazyInitializer.lazyVal)
    }
  }

  it should "catch a problem with companion objects" in {
    assertThrows[ExceptionInInitializerError] {
      Synchronizer.synchronize(LazyObject)
    }
  }

  it should "catch a problem with exception handlers" in {
    assertThrows[SynchronizationException] {
      Synchronizer.synchronize {
        try {
          throw new Exception()
        }
        catch {
          case _ => Synchronizer.synchronize() // try again
        }
      }
    }
  }
}

class SynchronizationException extends RuntimeException

object LazyInitializer {

  lazy val lazyVal: Int = {
    Synchronizer.synchronize()
    42
  }
}

object LazyObjectRef {
  val lazyObject = LazyObject
}

object LazyObject {
  println("Started LazyObject initialization...")
  Synchronizer.synchronize()
  println("Finished LazyObject initialization...")
}

object Synchronizer {

  val synchronizing = new AtomicBoolean(false)

  def doNothing(): Unit = ()

  def synchronize(f: => Unit = doNothing): Unit = {
    Synchronizer.synchronized {
      val wasSynchronizing = synchronizing.getAndSet(true)

      println("Startd synchronizing...")
      try {
        if (wasSynchronizing) {
          throw new SynchronizationException()
        }
        f
        Thread.sleep(200)
      }
      finally {
        synchronizing.set(false)
        println("Finished synchronizing...")
      }
    }
  }
}
