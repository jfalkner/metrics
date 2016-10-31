package falkner.jayson.metrics.io

import java.nio.file.{Files, Path}

import falkner.jayson.metrics._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.JavaConverters._

/**
  * Utility code for writing CSV exports of the data
  *
  * This code isn't yet RFC 4180 compliant -- https://tools.ietf.org/html/rfc4180
  *
  * It should work fine as-is, but will be improved later.
  */
object CSV {

  case class Chunk(namespace: String, h: Seq[String], c: Seq[String]) {
    lazy val headers = h.map(v => s"$namespace: $v").mkString(",")
    lazy val values = c.mkString(",")
    lazy val all = headers + "\n" + values
    lazy val map = h.zip(c).toMap
    lazy val headersNoNamespace = h.mkString(",")
  }

  case class Merged(chunks: Seq[Chunk]) {
    lazy val headers = chunks.map(_.headers).mkString(",")
    lazy val values = chunks.map(_.values).mkString(",")
    lazy val all = headers + "\n" + values
  }

  def apply(in: Path): Chunk = Seq(Files.readAllLines(in).asScala).map(l => CSV(l)).head

  def apply(ns: String, lines: Seq[String]): Chunk = Chunk(ns, lines(0).split(","), lines(1).split(","))

  def apply(lines: Seq[String]): Chunk = Chunk(lines(0).split(":")(0), lines(0).split(",").map(_.split(": ").tail.mkString(": ")), lines(1).split(","))

  def merge(mls: Chunk*): Merged = Merged(mls)

  def stack(ms: Merged*): String = ms.head.headers + "\n" + ms.map(_.values).mkString("\n")

  def apply(out: Path, m: Metrics): Path = CSV(out, Seq(m))

  def apply(out: Path, mls: Seq[Metrics]): Path = Files.write(out, CSV(mls :_ *).all.getBytes)

  def apply(mls: Metrics*): Merged = Merged(mls.map(ml => Future(CSV(ml))).map(f => Await.result(f, Duration.Inf)))

  def apply(ml: Metrics): Chunk =
    Seq(ml).map(_.values.flatMap(m => export(m)).unzip).map(u => Chunk(ml.namespace, u._1, u._2.map(escape))).head

  def export(m: Metric, prefix: String = ""): List[(String, String)] = m match {
    case d: Metrics => d.values.flatMap(v => export(v, d.name + ": "))
    case n: Num => List((s"$prefix${n.name}", blankIfError(n.value)))
    case s: Str => List((s"$prefix${s.name}", blankIfError(s.value)))
    case b: Bool => List((s"$prefix${b.name}", blankIfError(b.value.toString)))
    case _ => Nil // skip serializing arrays
  }

  def blankIfError(f: => String): String = Try(f) match {
    case Success(s) => s
    case Failure(t) => ""
  }

  def escape(s: String): String = s.replace(",", "").replace("\n", "").replace("\r", "").replace("\t", " ").trim
}
