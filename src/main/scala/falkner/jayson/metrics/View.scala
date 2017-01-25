package falkner.jayson.metrics

/**
  * Represents a tabular view of metrics data with any naming and ordering desired
  *
  * `Metrics` classes expose a set of values and distributions, which can be flattened directly to CSV or kept as-is in
  * JSON format; however, most commonly users will want a subset of the values and in a custom order with custom names
  * for each column. A `View` is exactly that.
  *
  * Multiple `View` instances are a convenient solution for exposing the same data in multiple different tabular formats.
  * For example, supporting two different opinionated user groups or use cases.
  */
trait View {
  val name: String
  val description: String
  val metrics: List[Col]
}

