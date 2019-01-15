package org.clulab.fatdynet.utils

import edu.cmu.dynet._

object Transducer {

  def transduce(builder: RnnBuilder, inputs: Iterable[Expression]): Iterable[Expression] = {
    inputs.map(builder.addInput)
  }
}
