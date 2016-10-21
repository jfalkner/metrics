package falkner.jayson.metrics.io

import com.pacb.itg.metrics._
import spray.json.JsObject

/**
  * Exports data as JSON
  *
  * This is a superset of the CSV equivalent. The reason for this class is twofold:
  *
  * 1. Convenience for code that wants to parse JSON
  * 2. Include all values, even those that are not easily "flattened" for a CSV export
  */
object JSON {

  // serializes one movie context
  def write(metric: ExactValueMap): JsObject = JsObject(Seq(metric.json).flatten.toMap)
}
