package org.clulab.dynet.models.hot

import org.clulab.dynet.models.Sizeable

case class HotSizes(
  RNN_STATE_SIZE: Int,
  NONLINEAR_SIZE: Int,
  RNN_LAYERS: Int,
  CHAR_RNN_LAYERS: Int,
  CHAR_EMBEDDING_SIZE: Int,
  CHAR_RNN_STATE_SIZE: Int,

  w2i: Sizeable,
  t2i: Sizeable,
  c2i: Sizeable,
  embeddingDim: Int
)

object HotSizes {
  val RNN_STATE_SIZE = 50
  val NONLINEAR_SIZE = 32
  val RNN_LAYERS = 1
  val CHAR_RNN_LAYERS = 1
  val CHAR_EMBEDDING_SIZE = 32
  val CHAR_RNN_STATE_SIZE = 16

  val w2i = Sizeable(100)
  val t2i = Sizeable(230)
  val c2i = Sizeable(123)
  val embeddingDim = 300

  def newDefault(): HotSizes =
      HotSizes(RNN_STATE_SIZE, NONLINEAR_SIZE, RNN_LAYERS, CHAR_RNN_LAYERS, CHAR_EMBEDDING_SIZE, CHAR_RNN_STATE_SIZE,
          w2i, t2i, c2i, embeddingDim)
}
