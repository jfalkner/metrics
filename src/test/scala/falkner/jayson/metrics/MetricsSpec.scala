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
          // continuous and discrete dist
          "3,3,0.33333334,0.5,0.5,0.0,1.0," +
          "3,4,1,4.0,4,2,6," +
          // errors are exported as blanks
          ",,,"
      }
    }
    "JSON serialization works" in {
      withCleanup { (p) =>
        val json = new String(Files.readAllBytes(JSON.write(p, new TestMetrics()))).parseJson.asJsObject
        // arrays aren't serialized in CSV. check they appear in the JSON
        json.fields("NumArray") mustEqual JsArray(Vector(1, 2, 3).map(v => JsNumber(v)))
        json.fields("NumArrayFunc") mustEqual JsArray(Vector(2, 3, 4).map(v => JsNumber(v)))
        // errors are dropped from the JSON. check the known errors are missing
        json.fields.contains("IntArrayError") mustEqual false
        // distributions should appear as objects with calculated bins
        json.fields("DistContinuous") mustEqual JsObject(List(
          ("Samples", JsNumber(3)),
          ("Bins", JsNumber(3)),
          ("BinWidth", JsNumber(0.33333334)),
          ("Mean", JsNumber(0.5)),
          ("Median", JsNumber(0.5)),
          ("Min", JsNumber(0.0)),
          ("Max", JsNumber(1.0)),
          ("Bins", JsArray(Vector(1, 1, 1).map(v => JsNumber(v))))
        ): _*)
        json.fields("DistDiscrete") mustEqual JsObject(List(
          ("Samples", JsNumber(3)),
          ("Bins", JsNumber(4)),
          ("BinWidth", JsNumber(1)),
          ("Mean", JsNumber(4)),
          ("Median", JsNumber(4)),
          ("Min", JsNumber(2)),
          ("Max", JsNumber(6)),
          ("Bins", JsArray(Vector(1, 0, 1, 1).map(v => JsNumber(v))))
        ): _*)
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
    Dist("DistContinuous", calcContinuousDist(Seq(0f, 1f, 0.5f), nBins = 3, sort = true)),
    Dist("DistDiscrete", calcDiscreteDist(Seq(2, 4, 6), nBins = 4, sort = true)),
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

