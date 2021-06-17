package org.clulab.dynet.xor

import edu.cmu.dynet.ComputationGraph
import org.clulab.fatdynet.Test

import scala.concurrent.forkjoin.{ForkJoinPool => ScalaForkJoinPool}
import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.ParIterable
import scala.collection.parallel.ParSeq
import scala.collection.parallel.ParSet

class TestXor extends Test {

  object ThreadUtils {

    def parallelize[T](parIterable: ParIterable[T], threads: Int): ParIterable[T] = {
      // There seems to be no way other than code generation to avoid the deprecation warning.
      // At least it is limited to one location by being contained in a library method.
      // val forkJoinPool = new JavaForkJoinPool(threads) // For Scala 2.12
      val forkJoinPool = new ScalaForkJoinPool(threads)
      val forkJoinTaskSupport = new ForkJoinTaskSupport(forkJoinPool)

      parIterable.tasksupport = forkJoinTaskSupport
      parIterable
    }

    def parallelize[T](seq: Seq[T], threads: Int): ParSeq[T] = {
      val parSeq = seq.par
      parallelize(parSeq, threads)
      parSeq
    }

    def parallelize[T](set: Set[T], threads: Int): ParSet[T] = {
      val parSet = set.par
      parallelize(parSet, threads)
      parSet
    }
  }

  def test(name: String, f: Boolean => Float): Unit = {
    behavior of name

//    it should "run" in {
//      val loss = f(false)
//
//      loss should be(Xor.expectedLoss)
//    }

//    it should "run serially" in {
//      Range.inclusive(1, 8).foreach { _ =>
//        val loss = f(false)
//
//        loss should be(Xor.expectedLoss)
//      }
//    }

    it should "run in parallel" in {
      ThreadUtils.parallelize(Range.inclusive(1, 1000), 8).foreach { index =>
       val threadId = Thread.currentThread.getId
        println(s"Enter with index $index, threadId $threadId")
        val loss = f(true)
        if (loss.isNaN)
          println("loss is NaN!")
        loss should be(Xor.expectedLoss)
        println(s" Exit with index $index, threadId $threadId")
      }
      println(s"parallel end")
    }
  }
}
