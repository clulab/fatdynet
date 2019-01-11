package org.clulab.fatdynet.apps

import edu.cmu.dynet._
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Loader
import org.clulab.fatdynet.utils.Loader.ClosableModelSaver

case class XorParameters(p_W: Parameter, p_b: Parameter, p_V: Parameter, p_a: Parameter)

case class Stimulus(x1: Int, x2: Int, y0: Int) {

  def encode(x_values: FloatVector, y_value: FloatPointer): Unit = {
    x_values.update(0, x1)
    x_values.update(1, x2)
    y_value.set(y0)
  }
}

object XorExampleApp {
  val  INPUT_SIZE = 2
  val HIDDEN_SIZE = 8
  val OUTPUT_SIZE = 1

  val ITERATIONS = 10

  val stimuli = Array(
    // x1, x2, y0 = x1 ^ x2, where -1 = false, 1 = true
    Stimulus(-1, -1, -1),
    Stimulus(-1,  1,  1),
    Stimulus( 1, -1,  1),
    Stimulus( 1,  1, -1)
  )

  protected def mkPredictionGraph(xorParameters: XorParameters, x_values: FloatVector): Expression = {
    val W = Expression.parameter(xorParameters.p_W)
    val b = Expression.parameter(xorParameters.p_b)
    val V = Expression.parameter(xorParameters.p_V)
    val a = Expression.parameter(xorParameters.p_a)
    val x = Expression.input(Dim(x_values.length), x_values)

    V * (Expression.tanh(W * x + b)) + a
  }

  def train: (XorParameters, Array[Float]) = {
    val model = new ParameterCollection
    val sgd = new SimpleSGDTrainer(model) // i.e., stochastic gradient descent trainer

    val p_W = model.addParameters(Dim(HIDDEN_SIZE, INPUT_SIZE))
    val p_b = model.addParameters(Dim(HIDDEN_SIZE))
    val p_V = model.addParameters(Dim(OUTPUT_SIZE, HIDDEN_SIZE))
    val p_a = model.addParameters(Dim(OUTPUT_SIZE))
    val xorParameters = XorParameters(p_W, p_b, p_V, p_a)

    // Xs will be the input values; expression is created later in mkPredictionGraph.
    val x_values = new FloatVector(INPUT_SIZE)
    // Y will be the expected output value, which we _input_ from gold data.
    val y_value = new FloatPointer // because OUTPUT_SIZE is 1
    val y = Expression.input(y_value)

    val y_pred = mkPredictionGraph(xorParameters, x_values)
    val loss = Expression.squaredDistance(y_pred, y)

    println()
    println("Computation graphviz structure:")
    ComputationGraph.printGraphViz()

    def encode(stimulus: Stimulus, x_values: FloatVector, y_value: FloatPointer): Unit = {
      x_values.update(0, stimulus.x1)
      x_values.update(1, stimulus.x2)
      y_value.set(stimulus.y0)
    }

    // Train
    for (iter <- 0 to ITERATIONS - 1) {
      val loss_value = stimuli.map { stimulus =>
        encode(stimulus, x_values, y_value)

        val loss_value = ComputationGraph.forward(loss).toFloat

        ComputationGraph.backward(loss)
        sgd.update()
        loss_value
      }.sum / stimuli.length
      sgd.learningRate *= 0.998f
      println("iter = " + iter + ", loss = " + loss_value)
    }

    val results = preview(xorParameters, x_values, y_value, y_pred)
    (xorParameters, results)
  }

  protected def preview(xorParameters: XorParameters, x_values: FloatVector, y_value: FloatPointer, y_pred: Expression): Array[Float] = {
    println
    stimuli.map { stimulus =>
      stimulus.encode(x_values, y_value)
      ComputationGraph.forward(y_pred)
      val result = y_pred.value.toFloat
      println(s"STIMULUS = $stimulus, PRED = $result")
      result
    }
  }

  def preview(xorParameters: XorParameters): Array[Float] = {
    val x_values = new FloatVector(INPUT_SIZE)
    val y_value = new FloatPointer
    val y_pred = mkPredictionGraph(xorParameters, x_values)

    preview(xorParameters, x_values, y_value, y_pred)
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
    val (parameters, lookupParameters) = Loader.loadParameters(filename)
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
    ComputationGraph.renew
    val expectedResults = preview(xorParameters1)
    save(filename, xorParameters1)

    val xorParameters2 = load(filename)
    ComputationGraph.renew
    val actualResults = preview(xorParameters2)

    assert(initialResults.deep == expectedResults.deep)
    assert(expectedResults.deep == actualResults.deep)
  }
}
