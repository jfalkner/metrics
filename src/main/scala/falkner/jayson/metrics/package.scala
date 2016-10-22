package falkner.jayson

import spray.json.{JsBoolean, JsNumber, JsString, JsValue}

import scala.collection.immutable.ListMap
import scala.util.{Failure, Success, Try}

package object metrics {

  trait Documented {
    val name: String
    val desc: String
  }

  trait TryString[A] extends Documented {
    def attempt(v: A): String

    def apply(v: A): String = Try(attempt(v)) match {
      case Success(s) => s
      case Failure(t) => ""
    }
  }

  // formatting types since values are kept as raw strings
  trait Metric {
    val name: String
  }
  abstract case class Num(name: String) extends Metric {
    def value: String
  }
  case class NumArray(name: String, values: Seq[AnyVal]) extends Metric
  abstract class Str(name: String) extends Metric {
    def value: String
  }
  abstract class Bool(name: String) extends Metric {
    def value: Boolean
  }
  case class Dist(name: String, samples: Num, binNum: Num, binWidth: Num, mean: Num, median: Num, min: Num, max: Num, bins: NumArray) extends Metric {
    val metrics: List[Metric] = List(
      samples,
      binNum,
      binWidth,
      mean,
      median,
      min,
      max,
      bins
    )
  }


  object Num {
    def apply(name: String, f: () => AnyVal): Metric = new Num(name) {
        def value = f().toString
    }

    def apply(name: String, v: AnyVal): Num = new Num(name) {
      def value = v.toString
    }
  }

  object NumArray {
    def apply(name: String, f: () => Seq[Int]): Metric = new NumArray(name, f())

    def apply(name: String, v: Seq[Int]): Metric = new NumArray(name, v)
  }

  object Str {
    def apply(n: String, f: () => String): Metric = new Str(n) {
        override val name = n
        override val value = f()
      }
  }

  object Bool {
    def apply(n: String, f: () => Boolean): Metric = new Bool(n) {
        val name = n
        override val value = f()
      }
  }

  object Dist {
    def apply(name: String, d: Distribution.Discrete) = new Dist(
      name,
      Num("Samples", d.sampleNum),
      Num("Bins", d.binNum),
      Num("BinWidth", d.binWidth),
      Num("Mean", d.mean),
      Num("Median", d.median),
      Num("Min", d.min),
      Num("Max", d.max),
      NumArray("Bins", d.bins))

    def apply(name: String, d: Distribution.Continuous) = new Dist(
      name,
      Num("Samples", d.sampleNum),
      Num("Bins", d.binNum),
      Num("BinWidth", d.binWidth),
      Num("Mean", d.mean),
      Num("Median", d.median),
      Num("Min", d.min),
      Num("Max", d.max),
      NumArray("Bins", d.bins))
  }


  /**
    * By default all parsing keeps exact values as strings and has helper methods to convert to typed
    *
    * This enables the code to output a CSV with the exact original value observed. There is no assumption or
    * requirement to parse the String to a (potentially lossy) value and then convert back.
    *
    * Any user of the Scala API will get the typed value that is auto-parsed, but can optionally invoke the respective
    * xxxString method if the raw String is desired.
    */
  trait Metrics {
    val values: List[Metric]
  }
}
