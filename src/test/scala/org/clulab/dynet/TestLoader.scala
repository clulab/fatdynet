package org.clulab.dynet

import org.clulab.fatdynet.FatdynetTest

import scala.io.Source
import scala.util.Using

class TestLoader extends FatdynetTest {
  val RNN_STATE_SIZE = 50
  val NONLINEAR_SIZE = 32
  val RNN_LAYERS = 1
  val CHAR_RNN_LAYERS = 1
  val CHAR_EMBEDDING_SIZE = 32
  val CHAR_RNN_STATE_SIZE = 16

  case class Sizeable(size: Int)

  val w2i = Sizeable(100)
  val t2i = Sizeable(230)
  val c2i = Sizeable(123)
  val embeddingDim = 300

  def textFromFile(filename: String): String = {
    Using.resource(Source.fromFile(filename)) { source =>
      source.mkString
    }
  }
}
