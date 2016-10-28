package falkner.jayson.metrics.io

import java.nio.file.{Files, Path}

import falkner.jayson.metrics._
import spray.json.{JsArray, JsBoolean, JsNumber, JsObject, JsString, JsValue}

import scala.util.{Failure, Success, Try}

/**
  * Exports data as JSON
  *
  * This is a superset of the CSV equivalent. The reason for this class is twofold:
  *
  * 1. Convenience for code that wants to parse JSON
  * 2. Include all values, even those that are not easily "flattened" for a CSV export
  *
  * JSON serialization is done using https://github.com/spray/spray-json
  */
object JSON {

  def write(out: Path, ml: Metrics): Path = Files.write(out, JsObject(export(ml.values): _*).prettyPrint.getBytes)

  def export(o: List[Metric]): List[(String, JsValue)] = o.flatMap(_ match {
    case d: Dist => noneIfError[JsObject]((d.name, JsObject(export(d.metrics): _*)))
    case n: Num => noneIfError[JsNumber]((n.name, JsNumber(n.value)))
    case s: Str => noneIfError[JsString]((s.name, JsString(s.value)))
    case b: Bool => noneIfError[JsBoolean]((b.name, JsBoolean(b.value)))
    case n: NumArray => noneIfError[JsArray]((n.name, JsArray(n.values.asInstanceOf[Seq[Int]].map(m => JsNumber(m)).toVector)))
    // flatten out categorical distributions and keep key order
    case cd: CatDist => noneIfError[JsObject](
      (cd.name, JsObject(
        List(("Name", JsString(cd.name)), ("Samples", JsNumber(cd.samples))) ++
          cd.bins.keys.map(k => cd.bins(k) match {
            case s: Short => (k, JsNumber(s))
            case i: Int => (k, JsNumber(i))
            case l: Long => (k, JsNumber(l))
            case f: Float => (k, JsNumber(f))
            case d: Double => (k, JsNumber(d))
          }): _*))
    )
  })

  def noneIfError[A](f: => (String, A)): Option[(String, A)] = Try(f) match {
    case Success(s) => Some(s)
    case Failure(t) => None
  }
}
