package falkner.jayson.metrics.io

import java.nio.file.{Files, Path, StandardOpenOption}

import com.pacb.itg.metrics._
s

/**
  * Utility code for writing CSV exports of the data
  *
  * This code isn't yet RFC 4180 compliant -- https://tools.ietf.org/html/rfc4180
  *
  * It should work fine as-is, but will be improved later.
  */
object CSV {

  case class Result(header: String, rows: Seq[String])

  // serializes one movie context
  def write(out: Path, metrics: Seq[ExactValueMap]): Path = {
    val all = metrics.flatMap(m => for ((k, v) <- m.csv) yield (k.name, v))
    val r = Result(all.map(_._1).map(escape).mkString(","), Seq(all.map(_._2).mkString(","))) // TODO: all for many rows from one writer
    Files.write(out, r.header.getBytes)
    Files.write(out, ("\n" + r.rows.mkString("\n")).getBytes, StandardOpenOption.APPEND))
    out
  }

  def escape(s: String): String = s.replace(",", "").replace("\n", "").replace("\r", "").replace("\t", " ").trim
}
