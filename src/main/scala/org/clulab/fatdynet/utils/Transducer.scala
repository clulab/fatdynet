package org.clulab.fatdynet.utils

import org.clulab.dynet._

object Transducer {

  def transduce(builder: RnnBuilder, inputs: Array[Expression]): Array[Expression] = {
    builder.startNewSequence()
    inputs.map(builder.addInput)
  }
}
