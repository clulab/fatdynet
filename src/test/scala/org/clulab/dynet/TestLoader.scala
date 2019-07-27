package org.clulab.dynet

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import scala.io.Source

class TestLoader extends FlatSpec with Matchers {
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
    val source = Source.fromFile(filename)
    val text = source.mkString

    source.close()
    text
  }
}