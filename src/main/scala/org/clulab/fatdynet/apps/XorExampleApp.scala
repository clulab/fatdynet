package org.clulab.fatdynet.apps

import edu.cmu.dynet._
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Synchronizer
import org.clulab.fatdynet.utils.Utils

import scala.util.Random

case class XorModel(w: Parameter, b: Parameter, v: Parameter, a: Parameter, model: ParameterCollection)

case class XorTransformation(input1: Int, input2: Int, output: Int) {

  override def toString: String = getClass.getSimpleName + "((" + input1 + ", " + input2 + ") -> " + output + ")"

  // Testing
  def transform(inputValues: FloatVector): Unit = {
    inputValues.update(0, input1.toFloat)
    inputValues.update(1, input2.toFloat)
  }

  // Training
  def transform(inputValues: FloatVector, outputValue: FloatPointer): Unit = {
    transform(inputValues)
    outputValue.set(output.toFloat)
  }
}

class XorExample {
  protected val random: Random = new Random(1234L)

  val INPUT_SIZE = 2
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

  protected def mkPredictionGraph(xorModel: XorModel, xValues: FloatVector): Expression = {
    val x = Expression.input(Dim(xValues.length), xValues)

    val W = Expression.parameter(xorModel.w)
    val b = Expression.parameter(xorModel.b)
    val V = Expression.parameter(xorModel.v)
    val a = Expression.parameter(xorModel.a)
    val y = V * Expression.tanh(W * x + b) + a

    y
  }

  def train: (XorModel, Seq[Float]) = {
    println("train 1")
    val model = new ParameterCollection
    println("train ")
    val trainer = new SimpleSGDTrainer(model) // i.e., stochastic gradient descent trainer
    println("train 3")

    val WParameter = model.addParameters(Dim(HIDDEN_SIZE, INPUT_SIZE))
    println("train 4")
    val bParameter = model.addParameters(Dim(HIDDEN_SIZE))
    println("train 5")
    val VParameter = model.addParameters(Dim(OUTPUT_SIZE, HIDDEN_SIZE))
    println("train 6")
    val aParameter = model.addParameters(Dim(OUTPUT_SIZE))
    println("train 7")
    val xorModel = XorModel(WParameter, bParameter, VParameter, aParameter, model)
    println("train 8")

    // Xs will be the input values; the corresponding expression is created later in mkPredictionGraph.
    val xValues = new FloatVector(INPUT_SIZE)
    println("train 9")

    // Y will be the expected output value, which we _input_ from gold data.
    val yValue = new FloatPointer // because OUTPUT_SIZE is 1
    println("train 10")

    val results = Synchronizer.withComputationGraph("XorExampleApp.train()") {
      println("train 11")
      val yPrediction = mkPredictionGraph(xorModel, xValues)
      println("train 1")
      // This is done after mkPredictionGraph so that the values are not made stale by it.
      val y = Expression.input(yValue)
      println("train 13")
      val loss = Expression.squaredDistance(yPrediction, y)
      println("train 14")

      //    println()
      //    println("Computation graphviz structure:")
      //    ComputationGraph.printGraphViz()

      for (iteration <- 0 until ITERATIONS) {
        println("train 15")
        val lossValue = random.shuffle(transformations).map { transformation =>
          println("train 16")
          transformation.transform(xValues, yValue)
          println("train 17")

          val lossValue = ComputationGraph.forward(loss).toFloat()
          println("train 18")

          ComputationGraph.backward(loss)
          println("train 19")
          trainer.update()
          println("train 20")
          lossValue
        }.sum / transformations.length

        println("train 21")
        println(s"index = $iteration, loss = $lossValue")
        println("train 22")
        trainer.learningRate *= 0.999f
        println("train 23")
      }
      println("train 4")
      val results = predict(xorModel, xValues, yPrediction)
      println("train 25")
      results
    }
    println("train 26")

    (xorModel, results)
  }

  protected def predict(xorModel: XorModel, xValues: FloatVector, yPrediction: Expression): Seq[Float] = {
    var count = 0

    println()
    val result = transformations.map { transformation =>
      transformation.transform(xValues)
      // This is necessary in this version of the program, possibly because the values
      // of the input are changed without creating another ComputationGraph.
      val yValue = ComputationGraph.forward(yPrediction).toFloat()
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
    val xValues = new FloatVector(INPUT_SIZE)
    Synchronizer.withComputationGraph("XorExampleApp.predict()") {
      val yPrediction = mkPredictionGraph(xorModel, xValues)

      predict(xorModel, xValues, yPrediction)
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
}

  def run(args: Array[String]): Unit = {
    val filename = "XorModel.dat"

    Initializer.initialize(Map(Initializer.RANDOM_SEED -> 2522620396L))

    println("run One")
    val (xorModel1, initialResults) = train
    println("run Two")
    val expectedResults = predict(xorModel1)
    println("run Three")
    save(filename, xorModel1)

    println("run Four")
    val xorModel2 = load(filename)
    println("run Five")
    val actualResults = predict(xorModel2)
    println("run Six")

    assert(initialResults == expectedResults)
    assert(expectedResults == actualResults)
  }

  def main(args: Array[String]): Unit = {
    // Use this version only if nothing else will run afterwards.
    // Utils.shutdown(true) will trash DyNet.  Call run() instead,
    // if further operations will be performed.
    Utils.startup()
    run(args)
    Utils.shutdown(true)
  }
}
