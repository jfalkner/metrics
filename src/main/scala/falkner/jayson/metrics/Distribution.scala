package falkner.jayson.metrics

/**
  * Summarizes sets of values as distributions
  *
  * Helpful for exporting histogram
  */
object Distribution {

  case class Continuous(sampleNum: Int, binNum: Int, binWidth: Float, mean: Float, median: Float, min: Float, max: Float)

  case class Discrete(sampleNum: Int, binNum: Int, binWidth: Int, mean: Float, median: Int, min: Int, max: Int)

  def calcDiscreteDist(vals: Seq[Int], nBins: Int = 30, sort: Boolean = false): Discrete = sort match {
    case false => calcDiscreteDist(vals.sorted, nBins)
    case _ =>
      val min = vals.head
      val max = vals.last
      Discrete(
        vals.size,
        nBins,
        max / nBins,
        vals.sum.toFloat / vals.size,
        vals(vals.size / 2),
        min,
        max
      )
  }

  def calcContinuousDist(vals: Seq[Float], nBins: Int = 30, sort: Boolean = false): Continuous = sort match {
    case true => calcContinuousDist(vals.sorted, nBins)
    case _ =>
      val min = vals.head
      val max = vals.last
      Continuous(
        vals.size,
        nBins,
        max - min / nBins,
        vals.sum / vals.size,
        vals(vals.size / 2),
        min,
        max
      )
  }

  def print(title: String, d: Continuous): Unit = {
    println(s"$title: Continuous Distribution")
    println(s"  Num Samples: ${d.sampleNum}")
    println(s"  Num Bins: ${d.binNum}")
    println(s"  Bin Width: ${d.binWidth}")
    println(s"  Mean: ${d.mean}")
    println(s"  Median: ${d.median}")
    println(s"  Min: ${d.min}")
    println(s"  Max: ${d.max}")
  }

  def print(title: String, d: Discrete): Unit = {
    println(s"$title: Discrete Distribution")
    println(s"  Num Samples: ${d.sampleNum}")
    println(s"  Num Bins: ${d.binNum}")
    println(s"  Bin Width: ${d.binWidth}")
    println(s"  Mean: ${d.mean}")
    println(s"  Median: ${d.median}")
    println(s"  Min: ${d.min}")
    println(s"  Max: ${d.max}")
  }

  def printPercent(title: String, d: Continuous): Unit = {
    println(s"$title: Continuous Distribution")
    println(s"  Num Samples: ${d.sampleNum}")
    println(s"  Num Bins: ${d.binNum}")
    println(s"  Bin Width: ${d.binWidth}")
    println(f"  Mean: ${d.mean * 100}%.2f%%")
    println(f"  Median: ${d.median * 100}%.2f%%")
    println(f"  Min: ${d.min * 100}%.2f%%")
    println(f"  Max: ${d.max * 100}%.2f%%")
  }
}
