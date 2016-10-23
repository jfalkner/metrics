name := "Metrics"

version in ThisBuild := "0.0.4"

organization in ThisBuild := "falkner.jayson"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions in ThisBuild := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-language:postfixOps")

libraryDependencies ++= Seq(
    "org.specs2" %% "specs2-core" % "3.8.5" % "test",
    // JSON serialization support
    "io.spray" %%  "spray-json" % "1.3.2"
  )

// allow code coverage via - https://github.com/scoverage/sbt-scoverage
//coverageExcludedPackages := "<empty>;.*Export.*AsCSV.*" // don't cover the Util classes -- they should move to a branch
