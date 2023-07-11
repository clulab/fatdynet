package org.clulab.fatdynet.apps

import edu.cmu.dynet.examples.XorScala
import org.clulab.fatdynet.utils.Utils

object XorScalaApp {

  def main(args: Array[String]): Unit = {
    Utils.startup()
    XorScala.main(args)
    Utils.shutdown()
  }
}
