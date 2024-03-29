package org.clulab.fatdynet.apps

import edu.cmu.dynet._
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Synchronizer
import org.clulab.fatdynet.utils.Utils

import scala.util.Random
import scala.util.Using

object InternalLookupParameterExampleApp {

  case class XorModel(w: Parameter, b: Parameter, v: Parameter, a: Parameter, xParameter: LookupParameter, model: ParameterCollection)

  case class XorTransformation(index: Int, input1: Int, input2: Int, output: Int) {

    override def toString: String = getClass.getSimpleName + "((" + input1 + ", " + input2 + ") -> " + output + ")"

    def initialize(lookupParameter: LookupParameter): Unit = {
      lookupParameter.initialize(index, Seq(input1.toFloat, input2.toFloat))
    }

    // Testing
    def transform(constParameter: Expression): Expression = {
      Expression.pick(constParameter, index, 1L)
    }

    // Training
    def transform(outputValue: FloatPointer): Unit = {
      outputValue.set(output.toFloat)
    }
  }

  protected val random: Random = new Random(1234L)

  val  INPUT_SIZE = 2
  val HIDDEN_SIZE = 2
  val OUTPUT_SIZE = 1

  val ITERATIONS = 400

  val transformations: Seq[XorTransformation] = Seq(
    // index, input1, input2, output = input1 ^ input2
    XorTransformation(0, 0, 0, 0),
    XorTransformation(1, 0, 1, 1),
    XorTransformation(2, 1, 0, 1),
    XorTransformation(3, 1, 1, 0)
  )

  protected def mkPredictionGraph(xorModel: XorModel, xorTransformation: XorTransformation): Expression = {
    val W = Expression.parameter(xorModel.w)
    val b = Expression.parameter(xorModel.b)
    val V = Expression.parameter(xorModel.v)
    val a = Expression.parameter(xorModel.a)
    val allX = Expression.constParameter(xorModel.xParameter)
    val x = xorTransformation.transform(allX)
    val y = V * Expression.tanh(W * x + b) + a

    y
  }

  def train: (XorModel, Seq[Float]) = {
    val model = new ParameterCollection
    val trainer = new SimpleSGDTrainer(model) // i.e., stochastic gradient descent trainer

    val WParameter = model.addParameters(Dim(HIDDEN_SIZE, INPUT_SIZE))
    val bParameter = model.addParameters(Dim(HIDDEN_SIZE))
    val VParameter = model.addParameters(Dim(OUTPUT_SIZE, HIDDEN_SIZE))
    val aParameter = model.addParameters(Dim(OUTPUT_SIZE))
    val xParameter = model.addLookupParameters(transformations.size, Dim(INPUT_SIZE))
    transformations.foreach { _.initialize(xParameter) }
    val xorModel = XorModel(WParameter, bParameter, VParameter, aParameter, xParameter, model)

    // Y will be the expected output value, which we _input_ from gold data.
    val yValue = new FloatPointer // because OUTPUT_SIZE is 1

//    println()
//    println("Computation graphviz structure:")
//    ComputationGraph.printGraphViz()

    for (iteration <- 0 until ITERATIONS) {
      val lossValue = random.shuffle(transformations).map { transformation =>
        transformation.transform(yValue)
        Synchronizer.withComputationGraph("InternalLookupParameterExampleApp.train()") {
          val yPrediction = mkPredictionGraph(xorModel, transformation)
          val y = Expression.input(yValue)
          val loss = Expression.squaredDistance(yPrediction, y)
          val lossValue = loss.value().toFloat() // ComputationGraph.forward(loss).toFloat

          ComputationGraph.backward(loss)
          trainer.update()
          lossValue
        }
      }.sum / transformations.length

      println(s"index = $iteration, loss = $lossValue")
      trainer.learningRate *= 0.999f
    }

    val results = predict(xorModel)

    (xorModel, results)
  }

  protected def predict(xorModel: XorModel): Seq[Float] = {
    var count = 0

    println()
    val result = transformations.map { transformation =>
      val yValue = Synchronizer.withComputationGraph("InternalLookupParameterExampleApp.predict()") {
        val yPrediction = mkPredictionGraph(xorModel, transformation)
        val yValue = yPrediction.value().toFloat()
        yValue
      }
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

  def save(filename: String, xorModel: XorModel): Unit = {
    Using.resource(new CloseableModelSaver(filename)) { saver =>
      saver.addModel(xorModel.model, "/model")
    }
  }

  def load(filename: String): XorModel = {
    val repo = Repo(filename)
    val designs = repo.getDesigns()
    val model = repo.getModel(designs, "/model")

    val lookupParameter = model.getLookupParameter()
    val WParameter = model.getParameter()
    val bParameter = model.getParameter(1)
    val VParameter = model.getParameter(2)
    val aParameter = model.getParameter(3)

    XorModel(WParameter, bParameter, VParameter, aParameter, lookupParameter, model.getParameterCollection)
  }

  def run(args: Array[String]): Unit = {
    val filename = "XorModel.dat"

    Initializer.initialize(Map(Initializer.RANDOM_SEED -> 2522620396L))

    val (xorModel1, initialResults) = train

    val expectedResults = predict(xorModel1)
    save(filename, xorModel1)

    val xorModel2 = load(filename)
    val actualResults = predict(xorModel2)

    assert(initialResults == expectedResults)
    assert(expectedResults == actualResults)
  }

  def main(args: Array[String]): Unit = {
    Utils.startup()
    run(args)
    Utils.shutdown()
  }
}
