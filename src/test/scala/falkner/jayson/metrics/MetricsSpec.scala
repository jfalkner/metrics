package falkner.jayson.metrics

import java.nio.file.{Files, Path}

import org.specs2.mutable.Specification
import Distribution._
import spray.json._
import falkner.jayson.metrics.io.{CSV, JSON}
import org.specs2.matcher.MatchResult

/**
  * Specs2 Tests: https://etorreborre.github.io/specs2/
  *
  * Run these with `sbt clean coverage test coverageReport` and you should find 100% coverage of the lines of code. The
  * main use cases tested include the following.
  *
  * - Each metric type: String, Int, Float, Boolean, Array and Dist
  * - Each convenience companion class for the metrics
  * - Distribution calculations for Int and Float
  * - Blank values for metrics when errors are thrown
  *
  * These cover all the data types and companion classes for convenient use.
  */
class MetricsSpec extends Specification {

  "Metrics" should {
    "CSV serialization works" in {
      withCleanup { (p) =>
        val lines = Files.readAllLines(CSV.write(p, new TestMetrics()))
        lines.size mustEqual 2
        lines.get(0) mustEqual "String," +
          "NumExact,NumInt,NumFloat,NumExactFunc,NumIntFunc,NumFloatFunc," +
          "Boolean," +
          "DistContinuous: Samples,DistContinuous: Bins,DistContinuous: BinWidth,DistContinuous: Mean,DistContinuous: Median,DistContinuous: Min,DistContinuous: Max," +
          "DistDiscrete: Samples,DistDiscrete: Bins,DistDiscrete: BinWidth,DistDiscrete: Mean,DistDiscrete: Median,DistDiscrete: Min,DistDiscrete: Max," +
          "StringError,IntError,FloatError,BooleanError"
        lines.get(1) mustEqual "Bar," +
          "0.1,3,0.5,0.1,3,1.2," +
          "true," +
          "3,2,0.5,1.5,1.5,1.0,2.0," +
          "3,2,2,4.0,4,2,6," +
          // errors are exported as blanks
          ",,,"
      }
    }
    "JSON serialization works" in {
      withCleanup { (p) =>
        val json = new String(Files.readAllBytes(JSON.write(p, new TestMetrics()))).parseJson
        json.asJsObject.fields("NumArray") mustEqual JsArray(Vector(1, 2, 3).map(v => JsNumber(v)))
        json.asJsObject.fields("NumArrayFunc") mustEqual JsArray(Vector(2, 3, 4).map(v => JsNumber(v)))
        json.asJsObject.fields.contains("IntArrayError") mustEqual false
        // "NumArray,NumArrayFunc
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

class TestMetrics() extends Metrics {
  override lazy val values: List[Metric] = List(
    Str("String", () => "Bar"),
    Num("NumExact", "0.1"),
    Num("NumInt", 3),
    Num("NumFloat", 0.5f),
    Num("NumExactFunc", () => "0.1"),
    Num("NumIntFunc", () => 3),
    Num("NumFloatFunc", () => 1.2),
    Bool("Boolean", () => true),
    Dist("DistContinuous", calcContinuousDist(Seq(1f, 2f, 1.5f), nBins = 2, sort = true)),
    Dist("DistDiscrete", calcDiscreteDist(Seq(2, 4, 6), nBins = 2, sort = true)),
    NumArray("NumArray", Seq(1, 2, 3)),
    NumArray("NumArrayFunc", () => Seq(2, 3, 4)),
    // errors
    Str("StringError", errorString),
    Num("IntError", errorInt),
    Num("FloatError", errorFloat),
    Bool("BooleanError", errorBoolean),
    NumArray("IntArrayError", errorNumArray)
  )

  def error[A]: () => A = () => throw new Exception("Test Error")
  val errorBoolean = error[Boolean]
  val errorInt = error[Int]
  val errorFloat = error[Float]
  val errorString = error[String]
  val errorNumArray = error[Seq[Int]]
}

