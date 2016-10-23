package falkner.jayson

package object metrics {

  trait Metric {
    val name: String
  }

  trait Metrics {
    val values: List[Metric]
  }

  abstract case class Num(name: String) extends Metric {
    val value: () => String
  }
  abstract case class NumArray(name: String) extends Metric {
    val values: () => Seq[AnyVal]
  }
  abstract case class Str(name: String) extends Metric {
    val value: () => String
  }
  abstract case class Bool(name: String) extends Metric {
    val value: () => Boolean
  }
  case class Dist(name: String, samples: Num, binNum: Num, binWidth: Num, mean: Num, median: Num, min: Num, max: Num, bins: NumArray) extends Metric {
    val metrics: List[Metric] = List(
      samples,
      binNum,
      binWidth,
      mean,
      median,
      min,
      max,
      bins
    )
  }


  object Num {
    def apply(name: String, f: () => Any): Metric = new Num(name) {
        val value = () => f().toString
    }

    def apply(name: String, v: Int): Num = new Num(name) {
      val value = () => v.toString
    }

    def apply(name: String, v: Float): Num = new Num(name) {
      val value = () => v.toString
    }

    def apply(name: String, v: String): Num = new Num(name) {
      val value = () => v
    }
  }

  object NumArray {
    def apply(name: String, f: () => Seq[Int]): NumArray = new NumArray(name) {
      override val values = f
    }

    def apply(name: String, v: Seq[Int]): NumArray = new NumArray(name) {
      override val values = () => v
    }
  }

  object Str {
    def apply(n: String, f: () => String): Str = new Str(n) {
        override val value = f
      }
  }

  object Bool {
    def apply(n: String, f: () => Boolean): Bool = new Bool(n) {
        override val value = f
      }
  }

  object Dist {
    def apply(name: String, d: Distribution.Discrete) = new Dist(
      name,
      Num("Samples", d.sampleNum),
      Num("Bins", d.binNum),
      Num("BinWidth", d.binWidth),
      Num("Mean", d.mean),
      Num("Median", d.median),
      Num("Min", d.min),
      Num("Max", d.max),
      NumArray("Bins", d.bins))

    def apply(name: String, d: Distribution.Continuous) = new Dist(
      name,
      Num("Samples", d.sampleNum),
      Num("Bins", d.binNum),
      Num("BinWidth", d.binWidth),
      Num("Mean", d.mean),
      Num("Median", d.median),
      Num("Min", d.min),
      Num("Max", d.max),
      NumArray("Bins", d.bins))
  }
}
