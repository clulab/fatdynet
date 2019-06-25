package org.clulab.dynet.models.scala

import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.Parameter
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.RnnBuilder

case class HotModel(
  parameters: ParameterCollection,
  lookupParameters: LookupParameter,
  fwRnnBuilder: RnnBuilder,
  bwRnnBuilder: RnnBuilder,
  H: Parameter,
  O: Parameter,
  T: LookupParameter,
  charLookupParameters: LookupParameter,
  charFwRnnBuilder: RnnBuilder,
  charBwRnnBuilder: RnnBuilder
)
