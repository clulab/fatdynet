import ReleaseTransformations._

name := "fatdynet"

organization := "org.clulab"

val defaultScalaVersion = "2.12.4"

scalaVersion := defaultScalaVersion

crossScalaVersions := Seq("2.11.11", "2.12.4")

val majorMinor = {
  val versionRegex = "(\\d+)\\.(\\d+)\\.(\\d+)".r
  val versionRegex(major, minor, update) = defaultScalaVersion

  major + "." + minor
}

unmanagedBase := baseDirectory.value / ("lib-" + majorMinor)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

fork := true

envVars := Map("LD_LIBRARY_PATH" -> ".")

lazy val root = (project in file("."))

mainClass in Compile := Some("edu.cmu.dynet.examples.XorScala")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.withClassifier(Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)

//
// publishing settings
//
// publish to a maven repo
publishMavenStyle := true

// the standard maven repository
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

// let’s remove any repositories for optional dependencies in our artifact
pomIncludeRepository := { _ => false }

// mandatory stuff to add to the pom for publishing
pomExtra :=
<url>https://github.com/clulab/fatdynet</url>
<licenses>
  <license>
    <name>Apache License, Version 2.0</name>
    <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
    <distribution>repo</distribution>
  </license>
</licenses>
<scm>
  <url>https://github.com/clulab/fatdynet</url>
  <connection>https://github.com/clulab/fatdynet</connection>
</scm>
<developers>
  <developer>
    <id>mihai.surdeanu</id>
    <name>Mihai Surdeanu</name>
    <email>mihai@surdeanu.info</email>
  </developer>
</developers>

//
// end publishing settings
//

// release steps
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommandAndRemaining("sonatypeReleaseAll"),
  pushChanges
)
