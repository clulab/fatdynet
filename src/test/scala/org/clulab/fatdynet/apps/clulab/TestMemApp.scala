package org.clulab.fatdynet.apps.clulab

import org.clulab.dynet.ComputationGraph
import org.clulab.fatdynet.utils.Utils

object TestMemApp extends App {
  // Use a debug build of DyNet under Windows and also make
  // sure that environment variable MALLOC_TRACE is set.

  // This is set to leave a memory leak on purpose.  It includes a
  // secret message left in the memory.
  Utils.startup()

  // Stopping here will result in leaked computation graphs.
  ComputationGraph.renew()

  // Stopping here would leak if shutdown did not call Initializer.cleanup which uses
  // newComputationGraph = false when synchronizing.
  Utils.shutdown(true)
}
