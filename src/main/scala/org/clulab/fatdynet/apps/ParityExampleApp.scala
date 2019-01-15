package org.clulab.fatdynet.apps

import edu.cmu.dynet._
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Loader
import org.clulab.fatdynet.utils.Loader.ClosableModelSaver
import org.clulab.fatdynet.utils.Transducer

import scala.util.Random

case class ParityModel(w: Parameter, b: Parameter, v: Parameter, a: Parameter, model: ParameterCollection)

case class ParityTransformation(inputs: Array[Int], output: Int) {

  override def toString(): String = getClass.getSimpleName + "(" + inputs.mkString("(", ", ", ")") + " -> " + output.toString() + ")"

  // Testing
  def transform(inputValues: Array[Float]): Unit = {
    inputs.indices.foreach { index =>
      inputValues(index) = inputs(index)
    }
  }

  // Training
  def transform(inputValues: Array[Float], outputValue: FloatPointer): Unit = {
    transform(inputValues)
    outputValue.set(output)
  }
}

object ParityExampleApp {
  protected val random: Random = new Random(1234L)

  val LAYERS_SIZE = 1

  val  INPUT_SIZE = 1
  val HIDDEN_SIZE = 8
  val OUTPUT_SIZE = 1

  val ITERATIONS = 100

  val transformations: Seq[ParityTransformation] = Seq(
    // For oddParity
    ParityTransformation(Array(0), 1),
    ParityTransformation(Array(1), 0),

    ParityTransformation(Array(0, 0), 1),
    ParityTransformation(Array(0, 1), 0),
    ParityTransformation(Array(1, 0), 0),
    ParityTransformation(Array(1, 1), 1),

    ParityTransformation(Array(0, 0, 0), 1),
    ParityTransformation(Array(0, 0, 1), 0),
    ParityTransformation(Array(0, 1, 0), 0),
    ParityTransformation(Array(0, 1, 1), 1),
    ParityTransformation(Array(1, 0, 0), 0),
    ParityTransformation(Array(1, 0, 1), 1),
    ParityTransformation(Array(1, 1, 0), 1),
    ParityTransformation(Array(1, 1, 1), 0)
  )

  protected def mkPredictionGraph(parityModel: ParityModel, xValues: Seq[Float], builder: RnnBuilder): Expression = {
    // The graph will grow and grow without this next line.
    ComputationGraph.renew()
    // Use the new graph.
    builder.newGraph()

    val xs = xValues.map(Expression.input)
    val builderOutputs = Transducer.transduce(builder, xs)
    val builderOutput = builderOutputs.last

    val W = Expression.parameter(parityModel.w)
    val b = Expression.parameter(parityModel.b)
    val V = Expression.parameter(parityModel.v)
    val a = Expression.parameter(parityModel.a)

    val y = V * Expression.tanh(W * builderOutput + b) + a

    y
  }

  def train: (ParityModel, Seq[Float], RnnBuilder) = {
    val model = new ParameterCollection
    val trainer = new SimpleSGDTrainer(model) // i.e., stochastic gradient descent trainer

    val WParameter = model.addParameters(Dim(HIDDEN_SIZE, HIDDEN_SIZE))
    val bParameter = model.addParameters(Dim(HIDDEN_SIZE))
    val VParameter = model.addParameters(Dim(OUTPUT_SIZE, HIDDEN_SIZE))
    val aParameter = model.addParameters(Dim(OUTPUT_SIZE))

    val rnnModel = new ParameterCollection
    val builder = new LstmBuilder(LAYERS_SIZE, INPUT_SIZE, HIDDEN_SIZE, rnnModel)
    val parityModel = ParityModel(WParameter, bParameter, VParameter, aParameter, rnnModel)

    val yValue = new FloatPointer // because OUTPUT_SIZE is 1

    for (iteration <- 0 until ITERATIONS) {
      val lossValue = random.shuffle(transformations).map { transformation =>
        val xValues = new Array[Float](transformation.inputs.length)

        transformation.transform(xValues, yValue)

        val yPrediction = mkPredictionGraph(parityModel, xValues, builder)
        val y = Expression.input(transformation.output)

        val loss = Expression.squaredDistance(yPrediction, y)
        val loss_value = loss.value().toFloat()

//        println()
//        println(transformation)
//        println("Computation graphviz structure:")
//        ComputationGraph.printGraphViz()

        ComputationGraph.backward(loss)
        trainer.update()
        loss_value
      }.sum

      println(s"index = $iteration, loss = $lossValue")
      trainer.learningRate *= 0.998f
    }

    val results = predict(parityModel, builder)

    (parityModel, results, builder)
  }

  def predict(parityModel: ParityModel, builder: RnnBuilder): Seq[Float] = {
    println
    transformations.map { transformation =>
      val xValues = new Array[Float](transformation.inputs.length)

      transformation.transform(xValues)

      val yPrediction = mkPredictionGraph(parityModel, xValues, builder)
      val yValue = yPrediction.value().toFloat()

      println(s"TRANSFORMATION = $transformation, PREDICTION = $yValue")
      yValue
    }
  }

  def save(filename: String, parityModel: ParityModel): Unit = {
    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addParameter(parityModel.w, "/W")
      saver.addParameter(parityModel.b, "/b")
      saver.addParameter(parityModel.v, "/V")
      saver.addParameter(parityModel.a, "/a")
      saver.addModel(parityModel.model, "/model")
    }
  }

  def load(filename: String): (ParityModel, RnnBuilder) = {
    val (optionBuilder, optionModel, parameters, _) = Loader.loadLstm(filename)
    val WParameters = parameters("/W")
    val bParameters = parameters("/b")
    val VParameters = parameters("/V")
    val aParameters = parameters("/a")

    (ParityModel(WParameters, bParameters, VParameters, aParameters, optionModel.get), optionBuilder.get)
  }

  def main(args: Array[String]) {
    val filename = "ParityModel.dat"

    Initialize.initialize(Map("random-seed" -> 2522620396L))

    val (parityModel1, initialResults, builder1) = train
    val expectedResults = predict(parityModel1, builder1)
    save(filename, parityModel1)

    val (parityModel2, builder2) = load(filename)
    val actualResults = predict(parityModel2, builder2)

    assert(initialResults == expectedResults)
    assert(expectedResults == actualResults)
  }
}
