import ReleaseTransformations._
import Tests._

name := "fatdynet"

organization := "org.clulab"

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.11", "2.12.4")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

Test / parallelExecution := false

{
  def groupDynamic(tests: Seq[TestDefinition]) = {
    def newRunPolicy = SubProcess(ForkOptions())
    //def newRunPolicy = InProcess

    val staticTests = tests.filter(!_.name.contains(".TestDynamic"))
    val staticGroup = new Group("static", staticTests, newRunPolicy)

    val dynamicTests = tests.filter(_.name.contains(".TestDynamic"))
    val dynamicGroup = new Group("dynamic", dynamicTests, newRunPolicy)

    Seq(dynamicGroup, staticGroup)
  }

  testGrouping in Test := groupDynamic((definedTests in Test).value)
}

lazy val root = (project in file("."))

mainClass in Compile := Some("org.clulab.fatdynet.apps.XorScalaApp")

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
