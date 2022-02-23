[![Build Status](https://github.com/clulab/fatdynet/workflows/FatDynet%20CI/badge.svg)](https://github.com/clulab/fatdynet/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.clulab/fatdynet_2.12/badge.svg?dummy=unused)](https://maven-badges.herokuapp.com/maven-central/org.clulab/fatdynet_2.12)

# fatdynet

## Understanding by Example

*fatdynet* is being used by at least two other [clulab](https://github.com/clulab) projects, and sometimes the best
way to understand something is to see an example.  Here are some links for those who want to cut to the chase:

### [processors](https://github.com/clulab/processors)
* https://github.com/clulab/processors/blob/master/main/build.sbt#L21
* https://github.com/clulab/processors/blob/master/main/src/main/scala/org/clulab/sequences/LstmCrfMtl.scala

### [factuality](https://github.com/clulab/factuality/)
* https://github.com/clulab/factuality/blob/master/build.sbt#L16
* https://github.com/clulab/factuality/blob/master/src/main/scala/org/clulab/factuality/Factuality.scala


## Depending on This Project

To use fatdynet with `sbt` in your own project, first check the [releases page](https://github.com/clulab/fatdynet/releases) for the most recent version number and then make a `build.sbt` file like

```
name := "fatdynetClient"

organization := "org.clulab"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.clulab" %% "fatdynet" % "0.2.4"
)
```

It can be tested with a program like
```
package org.clulab.fatdynetClient.apps

import edu.cmu.dynet.examples.XorScala

object XorScalaClientApp extends App {
  XorScala.main(args)
}
```


## Cloning This Project

A clone of this project should work out of the box with `sbt`.  You can perform a quick check with `sbt test`
which should pass or `sbt run` which should produce output similar to the lines quoted below.

```
> git clone https://github.com/clulab/fatdynet
> cd fatdynet
> sbt
sbt: fatdynet> test
...
sbt: fatdynet> run
...
```

```
[info] Running org.clulab.fatdynet.apps.XorScalaApp
Running XOR example
[dynet] random seed: 2354967643
[dynet] allocating memory: 512MB
[dynet] memory allocation done.
Dynet initialized!

Computation graphviz structure:
digraph G {
  rankdir=LR;
  nodesep=.05;
  N0 [label="v0 = parameters({8,2}) @ 0x7fe79abb0478"];
  N1 [label="v1 = parameters({8}) @ 0x7fe79abb05a8"];
  N2 [label="v2 = parameters({1,8}) @ 0x7fe79abb06f8"];
  N3 [label="v3 = parameters({1}) @ 0x7fe79abb08a8"];
  N4 [label="v4 = constant({2})"];
  N5 [label="v5 = scalar_constant(0x7fe79abb9060)"];
  N6 [label="v6 = v0 * v4"];
  N0 -> N6;
  N4 -> N6;
  N7 [label="v7 = v6 + v1"];
  N6 -> N7;
  N1 -> N7;
  N8 [label="v8 = tanh(v7)"];
  N7 -> N8;
  N9 [label="v9 = v2 * v8"];
  N2 -> N9;
  N8 -> N9;
  N10 [label="v10 = v9 + v3"];
  N9 -> N10;
  N3 -> N10;
  N11 [label="v11 = || v10 - v5 ||^2"];
  N10 -> N11;
  N5 -> N11;
}

Training...
iter = 0, loss = 2.2457085
iter = 1, loss = 1.6314303
iter = 2, loss = 1.2347934
iter = 3, loss = 0.91187227
iter = 4, loss = 0.63016045
iter = 5, loss = 0.3049145
iter = 6, loss = 0.09319025
iter = 7, loss = 0.019082734
iter = 8, loss = 0.003395962
iter = 9, loss = 5.326783E-4
iter = 10, loss = 8.053323E-5
iter = 11, loss = 1.1818531E-5
iter = 12, loss = 1.7824146E-6
iter = 13, loss = 2.6764963E-7
iter = 14, loss = 4.260896E-8
iter = 15, loss = 6.514804E-9
iter = 16, loss = 1.1485586E-9
iter = 17, loss = 1.756435E-10
iter = 18, loss = 3.5814907E-11
iter = 19, loss = 4.943601E-12
iter = 20, loss = 1.2052581E-12
iter = 21, loss = 6.865619E-13
iter = 22, loss = 3.5704772E-13
iter = 23, loss = 2.1049829E-13
iter = 24, loss = 1.687539E-13
iter = 25, loss = 6.57252E-14
iter = 26, loss = 2.1316282E-14
iter = 27, loss = 7.1054274E-15
iter = 28, loss = 1.0658141E-14
iter = 29, loss = 7.1054274E-15
[success] Total time: 1 s, completed Sep 17, 2018 5:20:44 PM
```


## Building the Files for This Project

The source code used for the libraries of this project is based on
- dynet commit d9d3e80 (version 2.1)
- eigen commit b2e267d

with some modifications explained on the project's [wiki pages](https://github.com/clulab/fatdynet/wiki).

The build environment included
- java 1.8.0_162
- sbt 1.2.1

The files in lib-2.12 were built with
- scala 2.12.4
- `cmake .. -DSCALA_VERSION=2.12.4 -DEIGEN3_INCLUDE_DIR=../../eigen -DENABLE_CPP_EXAMPLES=ON -DENABLE_SWIG=ON`
- `make`

and those in lib-2.11 were built with
- scala 2.11.11
- `cmake .. -DSCALA_VERSION=2.11.11 -DEIGEN3_INCLUDE_DIR=../../eigen -DENABLE_CPP_EXAMPLES=ON -DENABLE_SWIG=ON`
- `make`

For more details see [Compiling DyNet for Scala](https://github.com/clulab/fatdynet/wiki/Compiling-DyNet-for-Scala).
