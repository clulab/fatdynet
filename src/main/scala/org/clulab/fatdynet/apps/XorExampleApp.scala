package org.clulab.fatdynet.apps

import edu.cmu.dynet._

import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Loader
import org.clulab.fatdynet.utils.Loader.ClosableModelSaver

case class XorParameters(p_W: Parameter, p_b: Parameter, p_V: Parameter, p_a: Parameter)

case class Transformation(input1: Int, input2: Int, output: Int) {

  def transform(input_values: FloatVector, output_value: FloatPointer): Unit = {
    input_values.update(0, input1)
    input_values.update(1, input2)
    output_value.set(output)
  }
}

object XorExampleApp {
  val  INPUT_SIZE = 2
  val HIDDEN_SIZE = 8
  val OUTPUT_SIZE = 1

  val ITERATIONS = 10

  val transformations = Array(
    // input1, input2, output = input1 ^ input2, where -1 = false, 1 = true
    Transformation(-1, -1, -1),
    Transformation(-1,  1,  1),
    Transformation( 1, -1,  1),
    Transformation( 1,  1, -1)
  )

  protected def mkPredictionGraph(xorParameters: XorParameters, x_values: FloatVector): Expression = {
    ComputationGraph.renew()

    val W = Expression.parameter(xorParameters.p_W)
    val b = Expression.parameter(xorParameters.p_b)
    val V = Expression.parameter(xorParameters.p_V)
    val a = Expression.parameter(xorParameters.p_a)
    val x = Expression.input(Dim(x_values.length), x_values)

    V * Expression.tanh(W * x + b) + a
  }

  def train: (XorParameters, Array[Float]) = {
    val model = new ParameterCollection
    val sgd = new SimpleSGDTrainer(model) // i.e., stochastic gradient descent trainer

    val p_W = model.addParameters(Dim(HIDDEN_SIZE, INPUT_SIZE))
    val p_b = model.addParameters(Dim(HIDDEN_SIZE))
    val p_V = model.addParameters(Dim(OUTPUT_SIZE, HIDDEN_SIZE))
    val p_a = model.addParameters(Dim(OUTPUT_SIZE))
    val xorParameters = XorParameters(p_W, p_b, p_V, p_a)

    // Xs will be the input values; the corresponding expression is created later in mkPredictionGraph.
    val x_values = new FloatVector(INPUT_SIZE)
    // Y will be the expected output value, which we _input_ from gold data.
    val y_value = new FloatPointer // because OUTPUT_SIZE is 1

    val y_pred = mkPredictionGraph(xorParameters, x_values)
    // This is done after mkPredictionGraph so that the values are not made stale by it.
    val y = Expression.input(y_value)
    val loss = Expression.squaredDistance(y_pred, y)

    println()
    println("Computation graphviz structure:")
    ComputationGraph.printGraphViz()

    // Train
    for (iter <- 0 until ITERATIONS) {
      val loss_value = transformations.map { transformation =>
        transformation.transform(x_values, y_value)

        val loss_value = ComputationGraph.forward(loss).toFloat()

        ComputationGraph.backward(loss)
        sgd.update()
        loss_value
      }.sum / transformations.length

      println(s"iter = $iter, loss = $loss_value")
      sgd.learningRate *= 0.998f
    }

    val results = predict(xorParameters, x_values, y_value, y_pred)

    (xorParameters, results)
  }

  protected def predict(xorParameters: XorParameters, x_values: FloatVector, y_value: FloatPointer, y_pred: Expression): Array[Float] = {
    println
    transformations.map { transformation =>
      transformation.transform(x_values, y_value)
      ComputationGraph.forward(y_pred)

      val result = y_pred.value().toFloat()

      println(s"TRANSFORMATION = $transformation, PREDICTION = $result")
      result
    }
  }

  def predict(xorParameters: XorParameters): Array[Float] = {
    val x_values = new FloatVector(INPUT_SIZE)
    val y_value = new FloatPointer
    val y_pred = mkPredictionGraph(xorParameters, x_values)

    predict(xorParameters, x_values, y_value, y_pred)
  }

  def save(filename: String, xorParameters: XorParameters): Unit = {
    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addParameter(xorParameters.p_W, "/W")
      saver.addParameter(xorParameters.p_b, "/b")
      saver.addParameter(xorParameters.p_V, "/V")
      saver.addParameter(xorParameters.p_a, "/a")
    }
  }

  def load(filename: String): XorParameters = {
    val (parameters, _) = Loader.loadParameters(filename)
    val p_W = parameters("/W")
    val p_b = parameters("/b")
    val p_V = parameters("/V")
    val p_a = parameters("/a")

    XorParameters(p_W, p_b, p_V, p_a)
  }

  def main(args: Array[String]) {
    val filename = "XorModel.dat"

    Initialize.initialize(Map("random-seed" -> 2522620396L))

    val (xorParameters1, initialResults) = train
    val expectedResults = predict(xorParameters1)
    save(filename, xorParameters1)

    val xorParameters2 = load(filename)
    val actualResults = predict(xorParameters2)

    assert(initialResults.deep == expectedResults.deep)
    assert(expectedResults.deep == actualResults.deep)
  }
}
