package org.clulab.fatdynet.apps

import edu.cmu.dynet.ComputationGraph
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Utils

object TestMemApp extends App {
  // Use a debug build of DyNet under Windows and also make
  // sure that environment variable MALLOC_TRACE is set.

  // This is set to leave a memory leak on purpose.  It includes a
  // secret message left in the memory.
  Utils.startup()

  // Parallel operation requires initialization before renewal.
  Initializer.initialize()
  // Stopping here will result in leaked computation graphs.
  ComputationGraph.renew()

  // Stopping here would leak if shutdown did not call Initializer.cleanup which uses
  // newComputationGraph = false when synchronizing.
  Utils.shutdown(true)
}
