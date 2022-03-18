package org.clulab.dynet.clulab

import org.clulab.dynet.ComputationGraph
import org.clulab.dynet.Expression
import org.clulab.dynet.ExpressionVector
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.utils.Initializer

class TestEmptyExpressionVector extends FatdynetTest {
  Initializer.initialize(Map(Initializer.DYNAMIC_MEM -> true, Initializer.FORWARD_ONLY -> 1))

  behavior of "empty expression vector"

  it should "not crash" in {
    implicit val cg = ComputationGraph.renew(true)

    val expressions = Seq.empty[Expression]
    val expressionVector = new ExpressionVector(expressions)

    val thrown = the [AssertionError] thrownBy {
      Expression.concatenate(expressionVector)
    }
    val message = thrown.getMessage
//    message should include ("std::logic_error")
//    message should include ("dynet::concatenate")
    message should include ("Operation requires > 0 expression arguments")
  }
}
