package falkner.jayson.metrics.io

import java.nio.file.{Files, Path}

import falkner.jayson.metrics._

import scala.util.{Failure, Success, Try}

/**
  * Utility code for writing CSV exports of the data
  *
  * This code isn't yet RFC 4180 compliant -- https://tools.ietf.org/html/rfc4180
  *
  * It should work fine as-is, but will be improved later.
  */
object CSV {

  def write(out: Path, ml: Metrics): Path = {
    val vals = ml.values.flatMap(m => export(m)).map(v => (escape(v._1), escape(v._2)))
    Files.write(out, Seq(vals.map(_._1), vals.map(_._2)).map(_.mkString(",")).mkString("\n").getBytes)
  }

  def export(m: Metric, prefix: String = ""): List[(String, String)] = m match {
    case d: Dist => d.metrics.flatMap(m => export(m, d.name + ": "))
    case n: Num => List((s"$prefix${n.name}", blankIfError(n.value)))
    case s: Str => List((s"$prefix${s.name}", blankIfError(s.value)))
    case b: Bool => List((s"$prefix${b.name}", blankIfError(() => b.value().toString)))
    case _ => Nil // skip serializing arrays
  }

  def blankIfError(f: () => String): String = Try(f()) match {
    case Success(s) => s
    case Failure(t) => ""
  }

  def escape(s: String): String = s.replace(",", "").replace("\n", "").replace("\r", "").replace("\t", " ").trim
}
