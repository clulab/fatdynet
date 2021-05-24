package org.clulab.fatdynet

import edu.cmu.dynet.ComputationGraph
import org.clulab.fatdynet.utils.Utils
import org.scalatest.flatspec.{AnyFlatSpec => FlatSpec}
import org.scalatest.matchers.should.Matchers

class TestMem extends FlatSpec with Matchers {
  // Use a debug build of DyNet under Windows and also make
  // sure that environment variable MALLOC_TRACE is set.

  // This is set to leave a memory leak on purpose.  It includes a
  // secret message left in the memory.
  Utils.startup()

  // Stopping here will result in leaked computation graphs.
  ComputationGraph.renew()

  // Stopping here would leak if shutdown did not call Initializer.cleanup which uses
  // newComputationGraph = false when synchronizing.
  Utils.shutdown()
}
