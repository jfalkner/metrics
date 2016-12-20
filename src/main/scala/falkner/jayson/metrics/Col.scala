package falkner.jayson.metrics

import falkner.jayson.metrics.io.CSV
import spray.json.JsValue


object Col {
  def value(map: Map[String, JsValue], c: Col): String = map.get(c.ns) match {
    case Some(nsm) => nsm.asJsObject.fields.get(c.metric) match {
      case Some(mm) => c.mapVal match {
        // if a dist val
        case Some(k) => mm.asJsObject.fields.get(k) match {
          case Some(v) => c.filter(CSV.escape(v.toString.stripSuffix("\"").stripPrefix("\"")))
          case None => ""
        }
        // if a plain mapped val
        case None => c.filter(CSV.escape(mm.toString.stripSuffix("\"").stripPrefix("\"")))
      }
      case None => ""
    }
    case None => ""
  }
}

case class Col(name: String, ns: String, metric: String, mapVal: Option[String] = None, filter: (String) => String = (s) => s)
