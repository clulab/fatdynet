package org.clulab.fatdynet.expr

import edu.cmu.dynet.Dim
import edu.cmu.dynet.{Expression => ScalaExpression}
import edu.cmu.dynet.FloatPointer
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.Parameter

class StaticExpressionFactory extends ExpressionFactory[FatExpression] {

  protected def newFatExpression(scalaExpression: ScalaExpression): FatExpression =
      new FatExpression(this, scalaExpression)

  def exprTimes(e1: FatExpression, e2: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.exprTimes(e1.scalaExpression, e2.scalaExpression))

  def exprPlus(e1: FatExpression, e2: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.exprPlus(e1.scalaExpression, e2.scalaExpression))

  def input(s: Float): FatExpression =
      newFatExpression(ScalaExpression.input(s))

  def input(fp: FloatPointer): FatExpression =
      newFatExpression(ScalaExpression.input(fp))

  def input(d: Dim, pdata: FloatVector): FatExpression =
      newFatExpression(ScalaExpression.input(d, pdata))

  def parameter(p: Parameter): FatExpression =
      newFatExpression(ScalaExpression.parameter(p))

  def tanh(e: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.tanh(e.scalaExpression))

  def squaredDistance(e1: FatExpression, e2: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.squaredDistance(e1.scalaExpression, e2.scalaExpression))

  def exprMinus(e: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.exprMinus(e.scalaExpression))

  def exprPlus(e1: FatExpression, x: Float): FatExpression =
      newFatExpression(ScalaExpression.exprPlus(e1.scalaExpression, x))

  def exprPlus(x: Float, e2: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.exprPlus(x, e2.scalaExpression))

  def exprMinus(e1: FatExpression, e2: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.exprMinus(e1.scalaExpression, e2.scalaExpression))

  def exprMinus(e1: FatExpression, x: Float): FatExpression =
      newFatExpression(ScalaExpression.exprMinus(e1.scalaExpression, x))

  def exprMinus(x: Float, e2: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.exprMinus(x, e2.scalaExpression))

  def exprTimes(e1: FatExpression, x: Float): FatExpression =
      newFatExpression(ScalaExpression.exprTimes(e1.scalaExpression, x))

  def exprTimes(x: Float, e2: FatExpression): FatExpression =
      newFatExpression(ScalaExpression.exprTimes(x, e2.scalaExpression))

  def exprDivide(e1: FatExpression, x: Float): FatExpression =
      newFatExpression(ScalaExpression.exprDivide(e1.scalaExpression, x))
}
