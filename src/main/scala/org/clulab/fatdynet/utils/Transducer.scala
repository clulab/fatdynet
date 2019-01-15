package org.clulab.fatdynet.utils

import edu.cmu.dynet._

object Transducer {

  def transduce(builder: RnnBuilder, inputs: Iterable[Expression]): Iterable[Expression] = {
    builder.startNewSequence()
    inputs.map(builder.addInput)
  }
}
