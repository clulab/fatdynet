package org.clulab.fatdynet.apps

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.examples.XorScala
import edu.cmu.dynet.internal.MemDebug
import edu.cmu.dynet.internal.dynet_swig.cleanup

object XorScalaApp {

  def main(args: Array[String]) {
    new MemDebug().leak()
    XorScala.main(args)
    ComputationGraph.reset()
    System.gc()
    cleanup()
    println("I am finished.")
  }
}
