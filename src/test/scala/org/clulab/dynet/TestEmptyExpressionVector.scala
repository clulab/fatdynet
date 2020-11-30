package org.clulab.dynet

import edu.cmu.dynet.Expression
import edu.cmu.dynet.ExpressionVector
import org.clulab.fatdynet.utils.Initializer
import org.scalatest._

class TestEmptyExpressionVector extends FlatSpec with Matchers {
  Initializer.initialize()

  behavior of "empty expression vector"

  it should "not crash" in {
    val expressions = Seq.empty[Expression]
    val expressionVector = new ExpressionVector(expressions)

    the [AssertionError] thrownBy {
      Expression.concatenate(expressionVector)
    }
  }
}