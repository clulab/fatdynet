name := "fatdynet"
description := "FatDynet packages DyNet for direct integration into Scala/Java environments on Linux, Mac, and Windows."

val scala211 = "2.11.12" // up to 2.11.12
val scala212 = "2.12.17" // up to 2.12.17
val scala213 = "2.13.10" // up to 2.13.10
val scala30  = "3.0.2"   // up to 3.0.2
val scala31  = "3.1.3"   // up to 3.1.3
val scala32  = "3.2.1"   // up to 3.2.1

ThisBuild / crossScalaVersions := Seq(scala212, scala211, scala213, scala30, scala31, scala32)
ThisBuild / scalaVersion := scala30

Compile / mainClass := Some("org.clulab.fatdynet.apps.XorScalaApp")

libraryDependencies ++= {
  // See https://index.scala-lang.org/scala/scala-parallel-collections/scala-parallel-collections.
  val parallelLibraries = {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, major)) if major <= 12 => Seq()
      case _ => Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4") // up to 1.0.4
    }
  }

  Seq(
    "org.scala-lang.modules" %% "scala-collection-compat"  % "2.6.0",        // up to 2.6.0
    "org.scalatest"          %% "scalatest"                % "3.2.10" % Test // up to 3.2.10
  ) ++ parallelLibraries
}

lazy val root = (project in file("."))
