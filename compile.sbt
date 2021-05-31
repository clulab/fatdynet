// This allows DyNet to be loaded and also unloaded during a run
// which in turn allows memory record keeping for that specific period.
ThisBuild / Compile / run / fork := true