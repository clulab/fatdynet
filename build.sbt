name := "fatdynet"
description := "FatDynet packages DyNet for direct integration into Scala/Java environments on Linux, Mac, and Windows."

val scala11 = "2.11.12" // up to 2.11.12
val scala12 = "2.12.15" // up to 2.12.15
val scala13 = "2.13.8"  // up to 2.13.8
val scala3  = "3.1.0"   // up to 3.1.0

ThisBuild / crossScalaVersions := Seq(scala12, scala11, scala13, scala3)
ThisBuild / scalaVersion := crossScalaVersions.value.head

Compile / mainClass := Some("org.clulab.fatdynet.apps.XorScalaApp")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.10" % Test // Up to 3.2.10.
)

lazy val root = (project in file("."))
