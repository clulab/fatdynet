package org.clulab.dynet.models.java

import edu.cmu.dynet.internal.LookupParameter
import edu.cmu.dynet.internal.Parameter
import edu.cmu.dynet.internal.ParameterCollection
import edu.cmu.dynet.internal.RNNBuilder

case class HotModel(
  parameters: ParameterCollection,
  lookupParameters: LookupParameter,
  fwRnnBuilder: RNNBuilder,
  bwRnnBuilder: RNNBuilder,
  H: Parameter,
  O: Parameter,
  T: LookupParameter,
  charLookupParameters: LookupParameter,
  charFwRnnBuilder: RNNBuilder,
  charBwRnnBuilder: RNNBuilder
)
