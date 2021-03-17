name := "fatdynet"
description := "FatDynet packages DyNet for direct integration into Scala/Java environments on Linux, Mac, and Windows."

val scala11 = "2.11.12" // up to 2.11.12
val scala12 = "2.12.13" // up to 2.12.13
val scala13 = "2.13.5"  // up to 2.13.5

ThisBuild / crossScalaVersions := Seq(scala12, scala11, scala13)
ThisBuild / scalaVersion := crossScalaVersions.value.head

Compile / mainClass := Some("org.clulab.fatdynet.apps.XorScalaApp")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.5" % Test // Up to 3.2.5.
)

lazy val root = (project in file("."))
