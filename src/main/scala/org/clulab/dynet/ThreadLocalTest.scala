package org.clulab.dynet

import java.util.function.Supplier

import scala.util.Random

class ThreadLocalTest(val rand: Random) extends Supplier[String] {
  override def get(): String = rand.nextInt().toString()
}

object ThreadLocalTest {
  val rand = new Random()
  lazy val localValue = ThreadLocal.withInitial(new ThreadLocalTest(rand))

  def main(args: Array[String]): Unit = {

    Range.inclusive(0, 10).par.foreach{_ =>
      println(Thread.currentThread().getId + "\t" + localValue.get())
      Thread.sleep(100)
    }
  }
}
