package org.clulab.dynet.models.hot.scala

import edu.cmu.dynet.Dim
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.LstmBuilder
import edu.cmu.dynet.Parameter
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.RnnBuilder
import org.clulab.dynet.models.hot.HotSizes

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

object HotModel {

  def apply(hotSizes: HotSizes = HotSizes.newDefault()): HotModel = {
    // This model is intended to closely resemble one in use at clulab.
    val hs = hotSizes
    val parameters = new ParameterCollection()
    val lookupParameters = parameters.addLookupParameters(hs.w2i.size, Dim(hs.embeddingDim))
    val embeddingSize = hs.embeddingDim + 2 * hs.CHAR_RNN_STATE_SIZE
    val fwBuilder = new LstmBuilder(hs.RNN_LAYERS, embeddingSize, hs.RNN_STATE_SIZE, parameters)
    val bwBuilder = new LstmBuilder(hs.RNN_LAYERS, embeddingSize, hs.RNN_STATE_SIZE, parameters)
    val H = parameters.addParameters(Dim(hs.NONLINEAR_SIZE, 2 * hs.RNN_STATE_SIZE))
    val O = parameters.addParameters(Dim(hs.t2i.size, hs.NONLINEAR_SIZE))
    val T = parameters.addLookupParameters(hs.t2i.size, Dim(hs.t2i.size))

    val charLookupParameters = parameters.addLookupParameters(hs.c2i.size, Dim(hs.CHAR_EMBEDDING_SIZE))
    val charFwBuilder = new LstmBuilder(hs.CHAR_RNN_LAYERS, hs.CHAR_EMBEDDING_SIZE, hs.CHAR_RNN_STATE_SIZE, parameters)
    val charBwBuilder = new LstmBuilder(hs.CHAR_RNN_LAYERS, hs.CHAR_EMBEDDING_SIZE, hs.CHAR_RNN_STATE_SIZE, parameters)

    HotModel(parameters, lookupParameters, fwBuilder, bwBuilder, H, O, T,
        charLookupParameters, charFwBuilder, charBwBuilder)
  }
}
