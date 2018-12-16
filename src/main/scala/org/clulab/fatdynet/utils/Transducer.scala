package org.clulab.fatdynet.utils

import edu.cmu.dynet._

object Transducer {

  def transduce(builder: RnnBuilder, inputs: Iterable[Expression]): Option[Expression] =
      inputs.foldLeft(None: Option[Expression]){ (_, input) => Some(builder.addInput(input)) }
}
