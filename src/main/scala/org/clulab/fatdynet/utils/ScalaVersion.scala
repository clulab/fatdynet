package org.clulab.fatdynet.utils

import scala.util.Properties

case class ScalaVersion(major: Int, minor: Int) extends Ordered[ScalaVersion] {

  override def compare(that: ScalaVersion): Int = {
    lazy val majorResult = major - that.major
    lazy val minorResult = minor - that.minor

    if (majorResult != 0) majorResult
    else minorResult
  }
}

object ScalaVersion {
  val current = {
    val Array(major, minor, _) = Properties.versionNumberString.split('.').map(_.toInt)

    new ScalaVersion(major, minor)
  }
  val _2_11 = new ScalaVersion(2, 11)
  val _2_12 = new ScalaVersion(2, 12)
  val _2_13 = new ScalaVersion(2, 13)
}
