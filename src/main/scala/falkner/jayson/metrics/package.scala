package falkner.jayson

import scala.util.{Failure, Success, Try}

package object metrics {

  trait Metric {
    val name: String
  }

  trait Metrics {
    val namespace: String
    val version: String
    val values: List[Metric]

    def metric(name: String): Metric = values.filter(_.name == name).head

    def asString(name: String): String = metric(name) match {
      case n: Num => n.value
      case s: Str => s.value
    }

    def asBoolean(name: String): Boolean = metric(name) match {
      case b: Bool => b.value
    }

    def asSeqInt(name: String): Seq[Int] = metric(name) match {
      case n: NumArray => n.values.asInstanceOf[Seq[Int]]
    }
  }

  class Num(val name: String, callByValue: => String) extends Metric {
    def value = callByValue
  }

  class NumArray(val name: String, callByValues: => Seq[AnyVal]) extends Metric {
    def values = callByValues
  }

  class Str(val name: String, callByValue: => String) extends Metric {
    lazy val value = callByValue
  }

  class Bool(val name: String, callByValue: => Boolean) extends Metric {
    lazy val value = callByValue
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

  class CatDist(val name: String, val samples: Int, val bins: Map[String, AnyVal]) extends Metric

  object Num {
    def apply(name: String, value: => Any): Num = new Num(name, value.toString)
  }

  object NumArray {
    def apply(name: String, value: => Seq[Int]): NumArray = new NumArray(name, value)
  }

  object Str {
    def apply(name: String, value: => String): Str = new Str(name, value)
  }

  object Bool {
    def apply(name: String, value: => Boolean): Bool = new Bool(name, value)
  }

  object Dist {
    def apply(name: String, d: => Distribution.Discrete) = Try {
      new Dist(
        name,
        Num("Samples", d.sampleNum),
        Num("Bins", d.binNum),
        Num("BinWidth", d.binWidth),
        Num("Mean", d.mean),
        Num("Median", d.median),
        Num("Min", d.min),
        Num("Max", d.max),
        NumArray("Bins", d.bins))
    } match {
      case Success(d) => d
      // supports auto-calc of blank/place holder values -- TODO: restrict this to NullPointerException and bubble up rest?
      case Failure(_) =>
        new Dist(
          name,
          Num("Samples", ""),
          Num("Bins", ""),
          Num("BinWidth", ""),
          Num("Mean", ""),
          Num("Median", ""),
          Num("Min", ""),
          Num("Max", ""),
          NumArray("Bins", Seq()))
    }
  }

  object DistCon {

    def apply(name: String, d: => Distribution.Continuous) = Try {
      new Dist(
        name,
        Num("Samples", d.sampleNum),
        Num("Bins", d.binNum),
        Num("BinWidth", d.binWidth),
        Num("Mean", d.mean),
        Num("Median", d.median),
        Num("Min", d.min),
        Num("Max", d.max),
        NumArray("Bins", d.bins))
    } match {
      case Success(d) => d
      // supports auto-calc of blank/place holder values -- TODO: restrict this to NullPointerException and bubble up rest?
      case Failure(_) =>
        new Dist(
          name,
          Num("Samples", ""),
          Num("Bins", ""),
          Num("BinWidth", ""),
          Num("Mean", ""),
          Num("Median", ""),
          Num("Min", ""),
          Num("Max", ""),
          NumArray("Bins", Seq()))
    }
  }

  object CatDist {
    def apply(name: String, d: => Distribution.Categorical) = Try (new CatDist(name, d.sampleNum, d.bins)) match {
      case Success(d) => d
      case Failure(_) => new CatDist(name, 0, Map())
    }

    def apply(name: String, sampleNum: => Int, bins: => Map[String, AnyVal]) = Try (new CatDist(name, sampleNum, bins)) match {
      case Success(d) => d
      case Failure(_) => new CatDist(name, 0, Map())
    }
  }
}
