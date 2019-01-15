package org.clulab.fatdynet.apps

import edu.cmu.dynet._

import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Loader
import org.clulab.fatdynet.utils.Loader.ClosableModelSaver
import org.clulab.fatdynet.utils.Transducer

case class ParityModel(w: Parameter, b: Parameter, v: Parameter, a: Parameter, model: ParameterCollection)

case class ParityTransformation(inputs: Array[Int], output: Int) {

  // Testing
  def transform(inputValues: Array[FloatPointer]): Unit = {
    inputs.indices.foreach { index =>
      inputValues(index) = new FloatPointer()
      inputValues(index).set(inputs(index))
    }
  }

  // Training
  def transform(inputValues: Array[FloatPointer], outputValue: FloatPointer): Unit = {
    transform(inputValues)
    outputValue.set(output)
  }
}

object ParityExampleApp {
  val  INPUT_SIZE = 1
  val HIDDEN_SIZE = 8
  val OUTPUT_SIZE = 1

  val ITERATIONS = 100

  val transformations = Array(
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

  protected def mkPredictionGraph(parityModel: ParityModel, xValues: Array[FloatPointer], builder: RnnBuilder): Expression = {
    builder.newGraph()

    val W = Expression.parameter(parityModel.w)
    val b = Expression.parameter(parityModel.b)
    val V = Expression.parameter(parityModel.v)
    val a = Expression.parameter(parityModel.a)
    val xs = xValues.map(Expression.input)

    builder.startNewSequence() // maybe with xs already there, how to get only the last one?
    val builderOutputs = Transducer.transduce(builder, xs)
    val builderOutput = builderOutputs.last

    val y = V * Expression.tanh(W * builderOutput + b) + a

    y
  }

  def train: (ParityModel, Array[Float], RnnBuilder) = {
    val model = new ParameterCollection
    val trainer = new SimpleSGDTrainer(model) // i.e., stochastic gradient descent trainer

    val WParameter = model.addParameters(Dim(HIDDEN_SIZE, HIDDEN_SIZE))
    val bParameter = model.addParameters(Dim(HIDDEN_SIZE))
    val VParameter = model.addParameters(Dim(OUTPUT_SIZE, HIDDEN_SIZE))
    val aParameter = model.addParameters(Dim(OUTPUT_SIZE))

    val rnnModel = new ParameterCollection
    val builder = new LstmBuilder(1, INPUT_SIZE, HIDDEN_SIZE, rnnModel)

    val parityModel = ParityModel(WParameter, bParameter, VParameter, aParameter, rnnModel)

    val yValue = new FloatPointer // because OUTPUT_SIZE is 1

    // Train
    for (iteration <- 0 until ITERATIONS) {
      val lossValue = transformations.map { transformation =>
        val xValues = new Array[FloatPointer](transformation.inputs.length)

        transformation.transform(xValues, yValue)

        val yPrediction = mkPredictionGraph(parityModel, xValues, builder)
        val y = Expression.input(transformation.output)

        val loss = Expression.squaredDistance(yPrediction, y)
        val loss_value = loss.value().toFloat()

//        println()
//        println("Computation graphviz structure:")
//        ComputationGraph.printGraphViz()

        ComputationGraph.backward(loss)
        trainer.update()
        loss_value
      }.sum

      println(s"index = $iteration, loss = $lossValue")
//      trainer.learningRate *= 0.998f
    }

    val results = predict(parityModel, builder)

    (parityModel, results, builder)
  }

  def predict(parityModel: ParityModel, builder: RnnBuilder): Array[Float] = {
    println
    transformations.map { transformation =>
      val xValues = new Array[FloatPointer](transformation.inputs.length)

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

    assert(initialResults.deep == expectedResults.deep)
    assert(expectedResults.deep == actualResults.deep)
  }
}
