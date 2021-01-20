package org.clulab.fatdynet.utils

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.internal.MemDebug
import edu.cmu.dynet.internal.dynet_swig.cleanup

object Utils {

  def startup(): Unit = {
    val memDebug = new MemDebug()
    memDebug.leak_malloc()
//    memDebug.set_break(432) // If VS is not loaded, this will cause Java to crash.
  }

  def garbageCollect(): Unit = {
    System.gc()
    // Let other threads do the garbage collection?
    Thread.sleep(5000)
  }

  def shutdown(): Unit = {
    // This will release the global computation graph.
    ComputationGraph.renew()
    // So that it can be collected here.
    garbageCollect()
    // This will undermine the computation graph, so it had better be collected.
    ComputationGraph.reset();
    // Make sure everything else is gone before cleanup.
    garbageCollect()
    // Garbage collection must be finished or else this removes some
    // objects from underneath the still live Scala/Java objects.
    cleanup()
  }
}
