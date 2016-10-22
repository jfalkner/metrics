package falkner.jayson.metrics.io

import java.nio.file.{Files, Path}

import falkner.jayson.metrics._
import spray.json.{JsArray, JsBoolean, JsNumber, JsObject, JsString, JsValue}

/**
  * Exports data as JSON
  *
  * This is a superset of the CSV equivalent. The reason for this class is twofold:
  *
  * 1. Convenience for code that wants to parse JSON
  * 2. Include all values, even those that are not easily "flattened" for a CSV export
  */
object JSON {

  def write(out: Path, ml: Metrics): Path = Files.write(out, JsObject(export(ml.values)).prettyPrint.getBytes)

  def export(o: List[Metric]): List[(String, JsValue)] = o.map(_ match {
    case d: Dist => (d.name, JsObject(export(d.metrics)))
    case n: Num => (n.name, JsNumber(n.value))
    case s: Str => (s.name, JsString(s.value))
    case b: Bool => (b.name, JsBoolean(b.value))
    case n: NumArray => (n.name, JsArray(n.values.asInstanceOf[Seq[Int]].map(m => JsNumber(m)).toVector))
  })
}
