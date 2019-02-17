package org.clulab.fatdynet.utils

import edu.cmu.dynet.ModelSaver

class CloseableModelSaver(filename: String) extends ModelSaver(filename) {
  def close(): Unit = done
}
