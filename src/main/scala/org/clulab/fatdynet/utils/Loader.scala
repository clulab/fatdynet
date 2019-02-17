package org.clulab.fatdynet.utils

import edu.cmu.dynet.ModelLoader

class ClosableModelLoader(filename: String) extends ModelLoader(filename) {
  def close(): Unit = done
}
