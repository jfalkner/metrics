package falkner.jayson.metrics

trait View {
  val name: String
  val description: String
  val metrics: List[Col]
}

