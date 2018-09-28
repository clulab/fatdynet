package org.clulab.fatdynet.apps

import edu.cmu.dynet.examples.XorExample
import org.clulab.fatdynet.utils.Utils

object XorExampleApp {

  def main(args: Array[String]) {
    Utils.loadDynet()
    XorExample.main(args)
  }
}
