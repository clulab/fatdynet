name := "fatdynet"
description := "FatDynet packages DyNet for direct integration into Scala/Java environments on Linux, Mac, and Windows."

val scala11 = "2.11.12" // up to 2.11.12
val scala12 = "2.12.15" // up to 2.12.15
val scala13 = "2.13.8"  // up to 2.13.8
val scala3  = "3.1.0"   // up to 3.1.0

ThisBuild / crossScalaVersions := Seq(scala12, scala11, scala13, scala3)
ThisBuild / scalaVersion := crossScalaVersions.value.head

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
