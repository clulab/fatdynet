package org.clulab.fatdynet.expr

import edu.cmu.dynet.Dim
import edu.cmu.dynet.{Expression => ScalaExpression }
import edu.cmu.dynet.Tensor
import edu.cmu.dynet.internal.{Expression => JavaExpression}

class FatExpression(protected val expressionFactory: ExpressionFactory[FatExpression], val scalaExpression: ScalaExpression)
    extends Expressionable[FatExpression] {
  val javaExpression: JavaExpression = scalaExpression.expr

  def value(): Tensor = new Tensor(javaExpression.value)

  def dim(): Dim = new Dim(javaExpression.dim)

  def +(e2: FatExpression): FatExpression = {
    expressionFactory.exprPlus(this, e2)
  }

  def *(e2: FatExpression): FatExpression = {
    // Make sure that the bases are equal to each other by eq?
    expressionFactory.exprTimes(this, e2)
  }

  def -(e2: FatExpression): FatExpression = {
    expressionFactory.exprMinus(this, e2)
  }

  def +(r: Float): FatExpression = expressionFactory.exprPlus(this, r)

  def *(r: Float): FatExpression = expressionFactory.exprTimes(this, r)

  def -(r: Float): FatExpression = expressionFactory.exprMinus(this, r)

  def /(r: Float): FatExpression = expressionFactory.exprDivide(this, r)

  def unary_-(): FatExpression = expressionFactory.exprMinus(this)

  def debugString(): String = s"(Expression: ${dim().debugString()} ${value().toSeq()})"
}
