package org.clulab.fatdynet.utils

import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.Trainer

// A listener is used in lieu of using a logger.  The listener can decide how
// to do the logging or even halt further updating if it ever hears something.
trait TrainerListener {
  def listen(message: String): Unit
}

class ObliviousListener extends TrainerListener {
  def listen(message: String) = ()
}

class SafeTrainer(val trainer: Trainer, parameters: ParameterCollection, listener: TrainerListener = new ObliviousListener) {

  def update(): Unit = {
    try {
      trainer.update()
      // throw new RuntimeException("Magnitude of gradient is bad: inf")
    }
    catch {
      case exception: RuntimeException if exception.getMessage.startsWith("Magnitude of gradient is bad") =>
        // Aim to reset the gradient and continue training.
        parameters.resetGradient()
        listener.listen(s"Caught an invalid gradient exception: ${exception.getMessage}.  Resetting gradient L2 norm to ${parameters.gradientL2Norm()}.")
    }
  }
}

object SafeTrainer {

  def apply(trainer: Trainer, parameters: ParameterCollection, listener: TrainerListener = new ObliviousListener): SafeTrainer =
      new SafeTrainer(trainer, parameters, listener)
}
