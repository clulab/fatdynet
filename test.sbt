import Tests._

ThisBuild / Test / fork := true // also forces sequential operation
ThisBuild / Test / parallelExecution := false

def groupBySynchronizer(tests: Seq[TestDefinition]) = {
  def newRunPolicy = SubProcess(ForkOptions())

  val fatdynetTests = tests.filter(_.name.contains("org.clulab.fatdynet"))
  val dynetTests = tests.filter(_.name.contains("org.clulab.dynet.Test"))
  val lstmTests = tests.filter(_.name.contains("org.clulab.dynet.lstm"))
  val xorTests = tests.filter(_.name.contains("org.clulab.dynet.xor"))
  val soloTests = tests.filter(_.name.contains("org.clulab.dynet.solo"))
  val otherTests = {
    val groupedTests = (fatdynetTests ++ dynetTests ++ lstmTests ++ xorTests ++ soloTests).toSet
    tests.toSet.diff(groupedTests).toSeq
  }

  val fatdynetGroup = new Group("fatdynet", fatdynetTests, newRunPolicy)
  val dynetGroup = new Group("dynet", dynetTests, newRunPolicy)
  val lstmGroup = new Group("lstm", lstmTests, newRunPolicy)
  val xorGroup = new Group("xor", xorTests, newRunPolicy)
  val soloGroup = new Group("solo", soloTests, newRunPolicy)
  val otherGroup = new Group("other", otherTests, newRunPolicy)

  Seq(lstmGroup, xorGroup, soloGroup, fatdynetGroup, dynetGroup, otherGroup)
}

Test / testGrouping := groupBySynchronizer((Test / definedTests).value)
