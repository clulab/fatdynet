package org.clulab.fatdynet.expr

import edu.cmu.dynet.Dim
import edu.cmu.dynet.{Expression => ScalaExpression}
import edu.cmu.dynet.FloatPointer
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.Parameter
import org.clulab.fatdynet.cg.DynamicComputationGraph

class DynamicExpressionFactory(computationGraph: DynamicComputationGraph) extends ExpressionFactory[FatExpression] {

  protected def newFatExpression(scalaExpression: ScalaExpression): FatExpression =
    new FatExpression(this, scalaExpression)

  def exprTimes(e1: FatExpression, e2: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.exprTimes(e1.scalaExpression, e2.javaExpression))

  def exprPlus(e1: FatExpression, e2: FatExpression): FatExpression =
      newFatExpression(Expression.exprPlus(e1.javaExpression, e2.javaExpression))

  def input(s: Float): FatExpression =
      newFatExpression(Expression.input(s))

  def input(fp: FloatPointer): FatExpression =
      newFatExpression(Expression.input(fp))

  def input(d: Dim, pdata: FloatVector): FatExpression =
      newFatExpression(Expression.input(d, pdata))

  def parameter(p: Parameter): FatExpression =
      newFatExpression(Expression.parameter(p))

  def tanh(e: FatExpression): FatExpression =
      newFatExpression(Expression.tanh(e.javaExpression))

  def squaredDistance(e1: FatExpression, e2: FatExpression): FatExpression =
      newFatExpression(Expression.squaredDistance(e1.javaExpression, e2.javaExpression))

}
