val publication = "fatdynet"

ThisBuild / developers := List(
  Developer(
    id    = "mihai.surdeanu",
    name  = "Mihai Surdeanu",
    email = "mihai@surdeanu.info",
    url   = url("https://www.cs.arizona.edu/person/mihai-surdeanu")
  )
)
ThisBuild / homepage := Some(url(s"https://github.com/clulab/$publication"))
ThisBuild / licenses := List(
  "Apache License, Version 2.0" ->
      url("http://www.apache.org/licenses/LICENSE-2.0.html")
)
ThisBuild / organization := "org.clulab"
ThisBuild / organizationHomepage := Some(url("http://clulab.org/"))
ThisBuild / organizationName := "Computational Language Understanding (CLU) Lab"
// The sonatype plugin seems to overwrite these two values.
pomIncludeRepository := { _ => false }
publishMavenStyle := true
ThisBuild / publishTo := {
    val artifactory = "http://artifactory.cs.arizona.edu:8081/artifactory/"
    val repository = "sbt-release-local"
    val details =
        if (isSnapshot.value) ";build.timestamp=" + new java.util.Date().getTime
        else ""
    val location = artifactory + repository + details

    Some(("Artifactory Realm" at location).withAllowInsecureProtocol(true))
}
ThisBuild / scmInfo := Some(
  ScmInfo(
    url(s"https://github.com/clulab/$publication"),
    s"scm:git@github.com:clulab/$publication.git"
  )
)
