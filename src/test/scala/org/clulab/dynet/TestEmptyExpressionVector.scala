package org.clulab.dynet

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.Expression
import edu.cmu.dynet.ExpressionVector
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.utils.Initializer

class TestEmptyExpressionVector extends FatdynetTest {
  Initializer.cluInitialize()

  behavior of "empty expression vector"

  it should "not crash" in {
    val expressions = Seq.empty[Expression]
    implicit val cg = ComputationGraph.renew(true)
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
