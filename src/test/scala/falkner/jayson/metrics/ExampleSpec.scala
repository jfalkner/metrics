package falkner.jayson.metrics

import java.nio.file.{Files, Path}

import falkner.jayson.metrics.io.{CSV, JSON}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import collection.JavaConverters._
import falkner.jayson.metrics.Distribution.calcContinuous


/**
  * Example from the README.md
  *
  * Not intended for anything other than being a simple example of use. See MetricsSpec.scala for tests of all the main
  * use cases.
  */
class ExampleSpec extends Specification {

  class Example extends Metrics {
    override val namespace = "Example"
    override val version = "_"
    override lazy val values: List[Metric] = List(
      Str("Name", "Data Scientist"),
      Num("Age", "123"),
      DistCon("Data", calcContinuous(Seq(0f, 1f, 0.5f), nBins = 3, sort = true)),
      Num("Borken", throw new Exception("Calculation failed!"))
    )
  }

  "README.md example" should {
    "CSV export" in {
      withCleanup { (p) =>
        println("README.md Example CSV Export")
        Files.readAllLines(CSV(p, new Example())).asScala.foreach(println)
        1 mustEqual 1
      }
    }
    "JSON serialization works" in {
      withCleanup { (p) =>
        println("README.md Example JSON Export")
        println(new String(Files.readAllBytes(JSON.write(p, new Example()))))
        1 mustEqual 1
      }
    }
  }

  def withCleanup(f: (Path) => MatchResult[Any]): MatchResult[Any] = {
    val temp = Files.createTempFile("test", "tmp")
    try {
      f(temp)
    }
    finally {
      Files.delete(temp)
    }
  }
}



