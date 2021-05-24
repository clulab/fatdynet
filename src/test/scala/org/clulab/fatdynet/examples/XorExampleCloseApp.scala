package org.clulab.fatdynet.examples

// These components are made explicit so that one knows what to close().
import edu.cmu.dynet

import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Synchronizer
import org.clulab.fatdynet.utils.Utils

import scala.util.Random

case class XorModel(w: dynet.Parameter, b: dynet.Parameter, v: dynet.Parameter, a: dynet.Parameter, model: dynet.ParameterCollection) {
  def close(): Unit = {
    w.close()
    b.close()
    v.close()
    a.close()
    model.close()
  }
}

case class XorTransformation(input1: Int, input2: Int, output: Int) {

  override def toString: String = getClass.getSimpleName + "((" + input1 + ", " + input2 + ") -> " + output + ")"

  // Testing
  def transform(inputValues: dynet.FloatVector): Unit = {
    inputValues.update(0, input1)
    inputValues.update(1, input2)
  }

  // Training
  def transform(inputValues: dynet.FloatVector, outputValue: dynet.FloatPointer): Unit = {
    transform(inputValues)
    outputValue.set(output)
  }
}

object XorExampleApp {
  protected val random: Random = new Random(1234L)

  val  INPUT_SIZE = 2
  val HIDDEN_SIZE = 2
  val OUTPUT_SIZE = 1

  val ITERATIONS = 400

  val transformations: Seq[XorTransformation] = Seq(
    // input1, input2, output = input1 ^ input2
    XorTransformation(0, 0, 0),
    XorTransformation(0, 1, 1),
    XorTransformation(1, 0, 1),
    XorTransformation(1, 1, 0)
  )

  protected def mkPredictionGraph(xorModel: XorModel, xValues: dynet.FloatVector): dynet.Expression = {
    val x = dynet.Expression.input(dynet.Dim(xValues.length), xValues)

    val W = dynet.Expression.parameter(xorModel.w)
    val b = dynet.Expression.parameter(xorModel.b)
    val V = dynet.Expression.parameter(xorModel.v)
    val a = dynet.Expression.parameter(xorModel.a)
    val y = V * dynet.Expression.tanh(W * x + b) + a

    y
  }

  def train: (XorModel, Seq[Float]) = {
    val model = new dynet.ParameterCollection

    new dynet.SimpleSGDTrainer(model).autoClose { trainer => // i.e., stochastic gradient descent trainer
      val WParameter = model.addParameters(dynet.Dim(HIDDEN_SIZE, INPUT_SIZE))
      val bParameter = model.addParameters(dynet.Dim(HIDDEN_SIZE))
      val VParameter = model.addParameters(dynet.Dim(OUTPUT_SIZE, HIDDEN_SIZE))
      val aParameter = model.addParameters(dynet.Dim(OUTPUT_SIZE))
      val xorModel = XorModel(WParameter, bParameter, VParameter, aParameter, model)

      // Xs will be the input values; the corresponding expression is created later in mkPredictionGraph.
      val xValues = new dynet.FloatVector(INPUT_SIZE)
      // Y will be the expected output value, which we _input_ from gold data.
      val yValue = new dynet.FloatPointer // because OUTPUT_SIZE is 1

      val results = Synchronizer.withComputationGraph("XorExampleApp.train()") {
        val yPrediction = mkPredictionGraph(xorModel, xValues)
        // This is done after mkPredictionGraph so that the values are not made stale by it.
        val y = dynet.Expression.input(yValue)
        val loss = dynet.Expression.squaredDistance(yPrediction, y)

        //    println()
        //    println("Computation graphviz structure:")
        //    ComputationGraph.printGraphViz()

        for (iteration <- 0 until ITERATIONS) {
          val lossValue = random.shuffle(transformations).map { transformation =>
            transformation.transform(xValues, yValue)

            val lossValue = dynet.ComputationGraph.forward(loss).toFloat()

            dynet.ComputationGraph.backward(loss)
            trainer.update()
            lossValue
          }.sum / transformations.length

          println(s"index = $iteration, loss = $lossValue")
          trainer.learningRate *= 0.999f
        }
        val results = predict(xorModel, xValues, yPrediction)
        results
      }
      (xorModel, results)
    }
  }

  protected def predict(xorModel: XorModel, xValues: dynet.FloatVector, yPrediction: dynet.Expression): Seq[Float] = {
    var count = 0

    println
    val result = transformations.map { transformation =>
      transformation.transform(xValues)
      // This is necessary in this version of the program, possibly because the values
      // of the input are changed without creating another ComputationGraph.
      val yValue = dynet.ComputationGraph.forward(yPrediction).toFloat()
      val correct = transformation.output == yValue.round

      if (correct)
        count += 1
      println(s"TRANSFORMATION = $transformation, PREDICTION = $yValue, CORRECT = $correct")
      yValue
    }
    val accuracy = count / transformations.size.toFloat

    println(s"Accuracy: $count / ${transformations.size} = $accuracy")
    result
  }

  def predict(xorModel: XorModel): Seq[Float] = {
    new dynet.FloatVector(INPUT_SIZE).autoClose { xValues =>
      Synchronizer.withComputationGraph("XorExampleApp.predict()") {
        mkPredictionGraph(xorModel, xValues).autoClose { yPrediction =>
          predict(xorModel, xValues, yPrediction)
        }
      }
    }
  }

  def save(filename: String, xorModel: XorModel): Unit = {
    new CloseableModelSaver(filename).autoClose { saver =>
      saver.addModel(xorModel.model, "/model")
    }
  }

  def load(filename: String): XorModel = {
    val repo = Repo(filename)
    val designs = repo.getDesigns()
    val model = repo.getModel(designs, "/model")

    val WParameter = model.getParameter()
    val bParameter = model.getParameter(1)
    val VParameter = model.getParameter(2)
    val aParameter = model.getParameter(3)

    XorModel(WParameter, bParameter, VParameter, aParameter, model.getParameterCollection)
  }

  def run(args: Array[String]): Unit = {
    val filename = "XorModel.dat"

    Initializer.initialize(Map(Initializer.RANDOM_SEED -> 2522620396L))

    val (xorModel1, initialResults) = train
    val expectedResults = xorModel1.autoClose { xorModel1 =>
      val expectedResults = predict(xorModel1)
      save(filename, xorModel1) // TODO: just once
      expectedResults
    }
    val actualResults = load(filename).autoClose { xorModel2 =>
      predict(xorModel2)
    }

    assert(initialResults == expectedResults)
    assert(expectedResults == actualResults)
  }

  def main(args: Array[String]): Unit = {
    Utils.startup()
    run(args)
    Utils.shutdown()
  }
}
