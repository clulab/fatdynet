package org.clulab.dynet

import edu.cmu.dynet.FloatVector
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.utils.Initializer

class TestFastFloatVector extends FatdynetTest {
  Initializer.initialize()

  def timeFloatVector(floatVector: Seq[Float], fast: Boolean): Long = {
    val startTime = System.currentTimeMillis()
    0.until(10000).foreach { index =>
      FloatVector(floatVector, fast)
    }
    val endTime = System.currentTimeMillis()
    val elapsed = endTime - startTime

    elapsed
  }

  behavior of "fast float vector"

  it should "be faster for arrays" in {
    val floats = new Array[Float](300)
    val fastElapsedTime = timeFloatVector(floats, true)
    val slowElapsedTime = timeFloatVector(floats, false)

    println(s"Fast array: $fastElapsedTime")
    println(s"Slow array: $slowElapsedTime")
    fastElapsedTime should be < (slowElapsedTime)
  }

  it should "be faster for lists" in {
    val floats = new Array[Float](300).toList
    val fastElapsedTime = timeFloatVector(floats, true)
    val slowElapsedTime = timeFloatVector(floats, false)

    println(s"Fast list: $fastElapsedTime")
    println(s"Slow list: $slowElapsedTime")
    fastElapsedTime should be < (slowElapsedTime)
  }
}
