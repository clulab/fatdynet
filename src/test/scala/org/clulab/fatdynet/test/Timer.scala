package org.clulab.fatdynet.test

class Timer(val description: String) {
  var elapsedTime: Option[Long] = None
  var startTime: Option[Long] = None

  def time[R](block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result: R = block // call-by-name
    val t1 = System.currentTimeMillis()

    elapsedTime = Some(t1 - t0)
    result
  }

  def start(): Unit = {
    val t0 = System.currentTimeMillis()

    startTime = Some(t0)
  }

  def stop(): Unit = {
    if (startTime.isDefined) {
      val t1 = System.currentTimeMillis()

      elapsedTime = Some(t1 - startTime.get)
    }
  }

  override def toString: String = {
    if (elapsedTime.isDefined)
      s"\tTime\t$description\t${diffToString(elapsedTime.get)}"
    else if (startTime.isDefined)
      s"\tStart\t$description\t${startTime.get}\tms"
    else
      s"\tTimer\t$description"
  }

  def diffToString(diff: Long): String = {
    val days = (diff / (1000 * 60 * 60 * 24)) / 1
    val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
    val mins = (diff % (1000 * 60 * 60)) / (1000 * 60)
    val secs = (diff % (1000 * 60)) / 1000
    val msecs = (diff % (1000 * 1)) / 1

    f"$days:$hours%02d:$mins%02d:$secs%02d.$msecs%03d"
  }
}