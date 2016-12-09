package falkner.jayson

import java.text.DecimalFormat

import scala.collection.immutable.ListMap
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

  case class Dist(name: String, samples: Num, binNum: Num, binWidth: Num, mean: Num, median: Num, min: Num, max: Num, bins: NumArray) extends Metric with Metrics {
    override val namespace: String = ""
    override val version = ""
    override val values: List[Metric] = List(
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

  class CatDist(val name: String, val samples: Long, val bins: Map[String, AnyVal], val keys: List[String]) extends Metric with Metrics {
    override val namespace: String = ""
    override val version = ""
    override val values: List[Metric] = List(
      Num("Samples", samples)
    ) ++ keys.map(k => Num(k, bins.getOrElse(k, "")))
  }

  object Num {
    val default = new DecimalFormat("#.####") // A better way in Scala to do this? f"v%.2f" has undesired pading 1.5 -> 1.50

    def apply(name: String, value: => Any): Num = apply(name, value, default)

    def apply(name: String, value: => Any, df: Option[DecimalFormat]): Num = df match {
      case Some(df) => apply(name, value, df)
      case None => apply(name, value)
    }

    def apply(name: String, value: => Any, df: DecimalFormat): Num = new Num(name, value match {
      case v: Float => df.format(v)
      case v: Double => df.format(v)
      case v: Int => df.format(v)
      case v: Long => df.format(v)
      case v: BigDecimal => df.format(v)
      case _ => value.toString
    })
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

    def apply(name: String, d: => Distribution.Discrete, df: DecimalFormat): Dist = apply(name, d, Some(df))

    def apply(name: String, d: => Distribution.Discrete, df: Option[DecimalFormat] = None): Dist = Try {
      d.sampleNum match {
        case sn if sn > 0 =>
          new Dist(
            name,
            Num("Samples", d.sampleNum),
            Num("Bins", d.binNum),
            Num("BinWidth", d.binWidth, df),
            Num("Mean", d.mean, df),
            Num("Median", d.median, df),
            Num("Min", d.min, df),
            Num("Max", d.max, df),
            NumArray("Bins", d.bins))
        case _ =>
          new Dist(
            name,
            Num("Samples", 0),
            Num("Bins",""),
            Num("BinWidth", ""),
            Num("Mean", ""),
            Num("Median", ""),
            Num("Min", ""),
            Num("Max", ""),
            NumArray("Bins", Seq()))
      }
    } match {
      case Success(d) => d
      // supports auto-calc of blank/place holder values -- TODO: restrict this to NullPointerException and bubble up rest?
      case Failure(t) =>
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

    def apply(name: String, d: => Distribution.Continuous, df: DecimalFormat): Dist = apply(name, d, Some(df))

    def apply(name: String, d: => Distribution.Continuous, df: Option[DecimalFormat] = None): Dist = Try {
      new Dist(
        name,
        Num("Samples", d.sampleNum),
        Num("Bins", d.binNum),
        Num("BinWidth", d.binWidth, df),
        Num("Mean", d.mean, df),
        Num("Median", d.median, df),
        Num("Min", d.min, df),
        Num("Max", d.max, df),
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
    def apply(name: String, d: => Distribution.Categorical, keys: List[String]) = Try (new CatDist(name, d.sampleNum, d.bins, keys)) match {
      case Success(d) => d
      case Failure(_) => new CatDist(name, 0, ListMap(keys.map(k => (k, 0)) :_ *), keys)
    }

    def apply(name: String, sampleNum: => Int, bins: => Map[String, AnyVal], keys: List[String]) = Try (new CatDist(name, sampleNum, bins, keys)) match {
      case Success(d) => d
      case Failure(_) => new CatDist(name, 0, Map(keys.map(k => (k, 0)) :_ *), keys)
    }
  }
}
