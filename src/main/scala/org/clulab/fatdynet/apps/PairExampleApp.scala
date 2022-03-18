package org.clulab.fatdynet.apps

import edu.cmu.dynet._
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Synchronizer
import org.clulab.fatdynet.utils.Transducer
import org.clulab.fatdynet.utils.Utils

import scala.util.Random

case class PairModel(w: Parameter, b: Parameter, v: Parameter, a: Parameter, rnnBuilder: RnnBuilder, model: ParameterCollection)

case class PairTransformation(inputs: Array[Int], output: Int) {

  override def toString: String = getClass.getSimpleName + "(" + inputs.mkString("(", ", ", ")") + " -> " + output.toString + ")"

  // Testing
  def transform(inputValues: Array[Float]): Unit = {
    inputs.indices.foreach { index =>
      inputValues(index) = inputs(index).toFloat
    }
  }

  // Training
  def transform(inputValues: Array[Float], outputValue: FloatPointer): Unit = {
    transform(inputValues)
    outputValue.set(output.toFloat)
  }
}

object PairExampleApp {
  protected val random: Random = new Random(1234L)

  val LAYERS_SIZE = 1

  val  INPUT_SIZE = 1
  val HIDDEN_SIZE = 4
  val OUTPUT_SIZE = 1

  val ITERATIONS = 200

  val transformations: Seq[PairTransformation] = Seq(
    // For pairs of ones anywhere in sequence
    PairTransformation(Array(0, 0), 0),
    PairTransformation(Array(0, 1), 0),
    PairTransformation(Array(1, 0), 0),
    PairTransformation(Array(1, 1), 1),

    PairTransformation(Array(0, 0, 0), 0),
    PairTransformation(Array(0, 0, 1), 0),
    PairTransformation(Array(0, 1, 0), 0),
    PairTransformation(Array(0, 1, 1), 1),
    PairTransformation(Array(1, 0, 0), 0),
    PairTransformation(Array(1, 0, 1), 0),
    PairTransformation(Array(1, 1, 0), 1),
    PairTransformation(Array(1, 1, 1), 1),

    PairTransformation(Array(0, 0, 0, 0), 0),
    PairTransformation(Array(0, 0, 0, 1), 0),
    PairTransformation(Array(0, 0, 1, 0), 0),
    PairTransformation(Array(0, 0, 1, 1), 1),
    PairTransformation(Array(0, 1, 0, 0), 0),
    PairTransformation(Array(0, 1, 0, 1), 0),
    PairTransformation(Array(0, 1, 1, 0), 1),
    PairTransformation(Array(0, 1, 1, 1), 1),
    PairTransformation(Array(1, 0, 0, 0), 0),
    PairTransformation(Array(1, 0, 0, 1), 0),
    PairTransformation(Array(1, 0, 1, 0), 0),
    PairTransformation(Array(1, 0, 1, 1), 1),
    PairTransformation(Array(1, 1, 0, 0), 1),
    PairTransformation(Array(1, 1, 0, 1), 1),
    PairTransformation(Array(1, 1, 1, 0), 1),
    PairTransformation(Array(1, 1, 1, 1), 1),

    PairTransformation(Array(0, 0, 0, 0, 0), 0),
    PairTransformation(Array(0, 0, 0, 0, 1), 0),
    PairTransformation(Array(0, 0, 0, 1, 0), 0),
    PairTransformation(Array(0, 0, 0, 1, 1), 1),
    PairTransformation(Array(0, 0, 1, 0, 0), 0),
    PairTransformation(Array(0, 0, 1, 0, 1), 0),
    PairTransformation(Array(0, 0, 1, 1, 0), 1),
    PairTransformation(Array(0, 0, 1, 1, 1), 1),
    PairTransformation(Array(0, 1, 0, 0, 0), 0),
    PairTransformation(Array(0, 1, 0, 0, 1), 0),
    PairTransformation(Array(0, 1, 0, 1, 0), 0),
    PairTransformation(Array(0, 1, 0, 1, 1), 1),
    PairTransformation(Array(0, 1, 1, 0, 0), 1),
    PairTransformation(Array(0, 1, 1, 0, 1), 1),
    PairTransformation(Array(0, 1, 1, 1, 0), 1),
    PairTransformation(Array(0, 1, 1, 1, 1), 1),

    PairTransformation(Array(1, 0, 0, 0, 0), 0),
    PairTransformation(Array(1, 0, 0, 0, 1), 0),
    PairTransformation(Array(1, 0, 0, 1, 0), 0),
    PairTransformation(Array(1, 0, 0, 1, 1), 1),
    PairTransformation(Array(1, 0, 1, 0, 0), 0),
    PairTransformation(Array(1, 0, 1, 0, 1), 0),
    PairTransformation(Array(1, 0, 1, 1, 0), 1),
    PairTransformation(Array(1, 0, 1, 1, 1), 1),
    PairTransformation(Array(1, 1, 0, 0, 0), 1),
    PairTransformation(Array(1, 1, 0, 0, 1), 1),
    PairTransformation(Array(1, 1, 0, 1, 0), 1),
    PairTransformation(Array(1, 1, 0, 1, 1), 1),
    PairTransformation(Array(1, 1, 1, 0, 0), 1),
    PairTransformation(Array(1, 1, 1, 0, 1), 1),
    PairTransformation(Array(1, 1, 1, 1, 0), 1),
    PairTransformation(Array(1, 1, 1, 1, 1), 1)
  )

  protected def mkPredictionGraph(pairModel: PairModel, xValues: Array[Float], builder: RnnBuilder)(implicit cg: ComputationGraph): Expression = {
    // The graph will grow and grow without this next line.
    // This is now handled by the Synchronizer.
    // ComputationGraph.renew()
    // Use the new graph.
    builder.newGraph()

    val xs = xValues.map(Expression.input)
    val builderOutputs = Transducer.transduce(builder, xs)
    val builderOutput = builderOutputs.last

    val W = Expression.parameter(pairModel.w)
    val b = Expression.parameter(pairModel.b)
    val V = Expression.parameter(pairModel.v)
    val a = Expression.parameter(pairModel.a)
    val y = V * Expression.tanh(W * builderOutput + b) + a

    y
  }

  def train: (PairModel, Seq[Float]) = {
    val model = new ParameterCollection
    val trainer = new SimpleSGDTrainer(model) // i.e., stochastic gradient descent trainer

    val WParameter = model.addParameters(Dim(HIDDEN_SIZE, HIDDEN_SIZE))
    val bParameter = model.addParameters(Dim(HIDDEN_SIZE))
    val VParameter = model.addParameters(Dim(OUTPUT_SIZE, HIDDEN_SIZE))
    val aParameter = model.addParameters(Dim(OUTPUT_SIZE))
    val rnnBuilder = new LstmBuilder(LAYERS_SIZE, INPUT_SIZE, HIDDEN_SIZE, model)
    val pairModel = PairModel(WParameter, bParameter, VParameter, aParameter, rnnBuilder, model)

    val yValue = new FloatPointer // because OUTPUT_SIZE is 1

    for (iteration <- 0 until ITERATIONS) {
      val lossValue = random.shuffle(transformations).map { transformation =>
      val xValues = new Array[Float](transformation.inputs.length)
        transformation.transform(xValues, yValue)
        Synchronizer.withComputationGraph("PairExampleApp.train()") { cg =>
          implicit val computationGraph = cg

          val yPrediction = mkPredictionGraph(pairModel, xValues, rnnBuilder)
          val y = Expression.input(transformation.output.toFloat)

          val loss = Expression.squaredDistance(yPrediction, y)
          val lossValue = loss.value().toFloat()

          //        println()
          //        println("Computation graphviz structure:")
          //        ComputationGraph.printGraphViz()

          cg.backward(loss)
          trainer.update()
          lossValue
        }
      }.sum

      println(s"index = $iteration, loss = $lossValue")
      trainer.learningRate *= 0.999f
    }

    val results = predict(pairModel, rnnBuilder)

    (pairModel, results)
  }

  def predict(pairModel: PairModel, builder: RnnBuilder): Seq[Float] = {
    var count = 0

    println()
    val result = transformations.map { transformation =>
      val xValues = new Array[Float](transformation.inputs.length)
      transformation.transform(xValues)

      val yValue = Synchronizer.withComputationGraph("PairExampleApp.predict()") { cg =>
        implicit val computationGraph = cg

        val yPrediction = mkPredictionGraph(pairModel, xValues, builder)
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

  def save(filename: String, pairModel: PairModel): Unit = {
    new CloseableModelSaver(filename).autoClose { saver =>
      saver.addModel(pairModel.model, "/model")
    }
  }

  def load(filename: String): PairModel = {
    val repo = Repo(filename)
    val designs = repo.getDesigns()
    val model = repo.getModel(designs, "/model")

    val WParameter = model.getParameter()
    val bParameter = model.getParameter(1)
    val VParameter = model.getParameter(2)
    val aParameter = model.getParameter(3)
    val rnnBuilder = model.getRnnBuilder()

    PairModel(WParameter, bParameter, VParameter, aParameter, rnnBuilder, model.getParameterCollection)
  }

  def run(args: Array[String]): Unit = {
    val filename = "PairModel.dat"

    Initializer.initialize(Map(Initializer.RANDOM_SEED -> 2522620396L))

    val (pairModel1, initialResults) = train
    val expectedResults = predict(pairModel1, pairModel1.rnnBuilder)
    save(filename, pairModel1)

    val pairModel2 = load(filename)
    val actualResults = predict(pairModel2, pairModel2.rnnBuilder)

    assert(initialResults == expectedResults)
    assert(expectedResults == actualResults)
  }

  def main(args: Array[String]): Unit = {
    Utils.startup()
    run(args)
    Utils.shutdown()
  }
}
