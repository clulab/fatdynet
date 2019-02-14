package org.clulab.fatdynet

import edu.cmu.dynet._

class Model(val name: String, val parameterCollection: ParameterCollection,
    val parameters: Seq[Parameter], val lookupParameters: Seq[LookupParameter], val rnnBuilders: Seq[RnnBuilder]) {
}
