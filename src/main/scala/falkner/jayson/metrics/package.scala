package falkner.jayson

import spray.json.JsValue

import scala.collection.immutable.ListMap
import scala.util.{Failure, Success, Try}

package object metrics {

  // so that you can ask what version initially added this
  trait AsOf {
    def version: String = "Unknown" // version
  }
  trait Documented extends AsOf {
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

  /**
    * By default all parsing keeps exact values as strings and has helper methods to convert to typed
    *
    * This enables the code to output a CSV with the exact original value observed. There is no assumption or
    * requirement to parse the String to a (potentially lossy) value and then convert back.
    *
    * Any user of the Scala API will get the typed value that is auto-parsed, but can optionally invoke the respective
    * xxxString method if the raw String is desired.
    */
  trait ExactValueMap {
    val csv: ListMap[Documented, String]
    val json: Seq[(String, JsValue)]
  }
}
