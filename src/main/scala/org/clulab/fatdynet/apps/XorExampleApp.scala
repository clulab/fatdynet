package org.clulab.fatdynet.apps

import edu.cmu.dynet._

import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Loader
import org.clulab.fatdynet.utils.Loader.ClosableModelSaver

case class XorModel(w: Parameter, b: Parameter, v: Parameter, a: Parameter)

case class XorTransformation(input1: Int, input2: Int, output: Int) {

  override def toString(): String = getClass.getSimpleName + "((" + input1 + ", " + input2 + ") -> " + output + ")"

  // Testing
  def transform(inputValues: FloatVector): Unit = {
    inputValues.update(0, input1)
    inputValues.update(1, input2)
  }

  // Training
  def transform(inputValues: FloatVector, outputValue: FloatPointer): Unit = {
    transform(inputValues)
    outputValue.set(output)
  }
}

object XorExampleApp {
  val  INPUT_SIZE = 2
  val HIDDEN_SIZE = 8
  val OUTPUT_SIZE = 1

  val ITERATIONS = 40

  val transformations = Array(
    // input1, input2, output = input1 ^ input2
    XorTransformation(0, 0, 0),
    XorTransformation(0, 1, 1),
    XorTransformation(1, 0, 1),
    XorTransformation(1, 1, 0)
  )

  protected def mkPredictionGraph(xorModel: XorModel, x_values: FloatVector): Expression = {
    ComputationGraph.renew()

    val W = Expression.parameter(xorModel.w)
    val b = Expression.parameter(xorModel.b)
    val V = Expression.parameter(xorModel.v)
    val a = Expression.parameter(xorModel.a)
    val x = Expression.input(Dim(x_values.length), x_values)
    val y = V * Expression.tanh(W * x + b) + a

    y
  }

  def train: (XorModel, Array[Float]) = {
    val model = new ParameterCollection
    val trainer = new SimpleSGDTrainer(model) // i.e., stochastic gradient descent trainer

    val WParameter = model.addParameters(Dim(HIDDEN_SIZE, INPUT_SIZE))
    val bParameter = model.addParameters(Dim(HIDDEN_SIZE))
    val VParameter = model.addParameters(Dim(OUTPUT_SIZE, HIDDEN_SIZE))
    val aParameter = model.addParameters(Dim(OUTPUT_SIZE))
    val xorModel = XorModel(WParameter, bParameter, VParameter, aParameter)

    // Xs will be the input values; the corresponding expression is created later in mkPredictionGraph.
    val xValues = new FloatVector(INPUT_SIZE)
    // Y will be the expected output value, which we _input_ from gold data.
    val yValue = new FloatPointer // because OUTPUT_SIZE is 1

    val yPrediction = mkPredictionGraph(xorModel, xValues)
    // This is done after mkPredictionGraph so that the values are not made stale by it.
    val y = Expression.input(yValue)
    val loss = Expression.squaredDistance(yPrediction, y)

    println()
    println("Computation graphviz structure:")
    ComputationGraph.printGraphViz()

    // Train
    for (iteration <- 0 until ITERATIONS) {
      val lossValue = transformations.map { transformation =>
        transformation.transform(xValues, yValue)

        val lossValue = ComputationGraph.forward(loss).toFloat()

        ComputationGraph.backward(loss)
        trainer.update()
        lossValue
      }.sum / transformations.length

      println(s"index = $iteration, loss = $lossValue")
      trainer.learningRate *= 0.998f
    }

    val results = predict(xorModel, xValues, yPrediction)

    (xorModel, results)
  }

  protected def predict(xorModel: XorModel, xValues: FloatVector, yPrediction: Expression): Array[Float] = {
    println
    transformations.map { transformation =>
      transformation.transform(xValues)
      ComputationGraph.forward(yPrediction)

      val yValue = yPrediction.value().toFloat()

      println(s"TRANSFORMATION = $transformation, PREDICTION = $yValue")
      yValue
    }
  }

  def predict(xorModel: XorModel): Array[Float] = {
    val xValues = new FloatVector(INPUT_SIZE)
    val yPrediction = mkPredictionGraph(xorModel, xValues)

    predict(xorModel, xValues, yPrediction)
  }

  def save(filename: String, xorModel: XorModel): Unit = {
    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addParameter(xorModel.w, "/W")
      saver.addParameter(xorModel.b, "/b")
      saver.addParameter(xorModel.v, "/V")
      saver.addParameter(xorModel.a, "/a")
    }
  }

  def load(filename: String): XorModel = {
    val (parameters, _) = Loader.loadParameters(filename)
    val WParameters = parameters("/W")
    val bParameters = parameters("/b")
    val VParameters = parameters("/V")
    val aParameters = parameters("/a")

    XorModel(WParameters, bParameters, VParameters, aParameters)
  }

  def main(args: Array[String]) {
    val filename = "XorModel.dat"

    Initialize.initialize(Map("random-seed" -> 2522620396L))

    val (xorModel1, initialResults) = train
    val expectedResults = predict(xorModel1)
    save(filename, xorModel1)

    val xorModel2 = load(filename)
    val actualResults = predict(xorModel2)

    assert(initialResults.deep == expectedResults.deep)
    assert(expectedResults.deep == actualResults.deep)
  }
}
