package org.clulab.fatdynet.test

import org.clulab.fatdynet.Test

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.parallel.immutable.ParRange

class TestSynchronization extends Test {
  val count = 8

  behavior of "a synchronized section"

  it should "work in serial" in {
    new ParRange(0.until(count)).foreach { _ =>
      Synchronizer.synchronize()
    }
  }

  it should "work in parallel" in {
    new ParRange(0.until(count)).foreach { _ =>
      Synchronizer.synchronize()
    }
  }

  it should "work with lazy vals" in {
    Synchronizer.synchronize(LazyInitializer.unsynchronizingLazyVal)
  }

  it should "catch a problem with lazy vals" in {
    assertThrows[SynchronizationException] {
      Synchronizer.synchronize(LazyInitializer.synchronizingLazyVal)
    }
  }

  it should "work with companion objects" in {
    Synchronizer.synchronize(UnsynchronizingLazyObject)
  }

  it should "catch a problem with companion objects" in {
    assertThrows[ExceptionInInitializerError] {
      Synchronizer.synchronize(SynchronizingLazyObject)
    }
  }

  it should "work with an exception handler" in {
    try {
      Synchronizer.synchronize {
        throw new Exception()
      }
    }
    catch {
      case _: Exception => Synchronizer.synchronize() // try again
    }
  }

  it should "catch a problem with exception handlers" in {
    assertThrows[SynchronizationException] {
      Synchronizer.synchronize {
        try {
          throw new Exception()
        }
        catch {
          case _: Exception => Synchronizer.synchronize() // try again
        }
      }
    }
  }

  it should "not initialize zero times" in {
    var initializationCount = 0
    val initialized = new AtomicBoolean(false)

    def block(): Unit = {
      if (!initialized.getAndSet(true)) {
        Thread.`yield`()
        Thread.sleep(1000)
        initializationCount += 1
      }
      println(s"returning with $initializationCount")
    }

    val results = new ParRange(0.until(2)).map { _ =>
      block()
      initializationCount
    }
    // This missed initialization should be avoided.
    results.min should be (0)
  }

  it should "initialize one time" in {
    var initializationCount = 0

    def block(): Unit = {
      if (initializationCount == 0) {
        Thread.`yield`()
        Thread.sleep(1000)
        initializationCount += 1
      }
    }

    new ParRange(0.until(2)).foreach { _ =>
      Synchronizer.synchronize {
        block()
      }
    }
    initializationCount should be (1)
  }

  it should "increment two times" in {
    val initializationCount = new AtomicInteger(0)

    def block(): Unit = {
      // This should be entered twice.  The second time, the first thread is sleeping and
      // has not yet been able to increment the count.  Then they will both have a chance to increment.
      if (initializationCount.get == 0) {
        Thread.`yield`()
        Thread.sleep(2000)
        initializationCount.incrementAndGet()
      }
    }

    new ParRange(0.until(2)).foreach { _ =>
      block()
    }
    // This double initialization should be avoided.
    initializationCount.get should be (2)
  }
}


class SynchronizationException extends RuntimeException

object LazyInitializer {

  lazy val unsynchronizingLazyVal: Int = {
    println("Started unsynchronizing lazy val initialization...")
    val result = 42
    println("Finished unsynchronizing lazy val initialization...")
    result
  }

  lazy val synchronizingLazyVal: Int = {
    println("Started synchronizing lazy val initialization...")
    Synchronizer.synchronize()
    val result = 42
    println("Finished synchronizing lazy val initialization...")
    result
  }
}

object UnsynchronizingLazyObject {
  println("Started UnsynchronizingLazyObject initialization...")
  println("Finished UnsynchronizingLazyObject initialization...")
}

object SynchronizingLazyObject {
  println("Started SynchronizingLazyObject initialization...")
  Synchronizer.synchronize()
  println("Finished SynchronizingLazyObject initialization...")
}

object Synchronizer {

  val synchronizing = new AtomicBoolean(false)

  def synchronize(f: => Unit = ()): Unit = {
    Synchronizer.synchronized {
      val wasSynchronizing = synchronizing.getAndSet(true)

      println("Started synchronizing...")
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
