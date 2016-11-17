package falkner.jayson.metrics

import scala.util.Sorting.stableSort

/**
  * Summarizes sets of values as distributions
  *
  * Helpful for exporting histogram
  */
object Distribution {

  case class Continuous(sampleNum: Int, binNum: Int, binWidth: Float, mean: Float, median: Float, min: Float, max: Float, bins: Seq[Int])

  case class Discrete(sampleNum: Int, binNum: Int, binWidth: Int, mean: Float, median: Int, min: Int, max: Int, bins: Seq[Int])

  case class Categorical(sampleNum: Int, bins: Map[String, AnyVal])

  def makeCategorical(vals: Map[String, Int]): Categorical = Categorical(vals.values.sum, vals)

  def calcCategorical(vals: Map[String, Traversable[Any]]): Categorical =
    Categorical(vals.values.map(_.size).sum, vals.map{ case (k, v) => (k, v.size) })

  def mean(vals: Seq[AnyVal]): Float = {
    var bd = BigDecimal(0)
    vals.foreach(_ match {
      case v: Long => bd += v
      case v: Int => bd += v
      case v: Short => bd += v.toInt
      case v: Float => bd += v.toDouble
      case v: Double => bd += v
    })
    bd /= vals.size
    bd.toFloat
  }

  def calcDiscrete(vals: Seq[Int], nBins: Int = 30, sort: Boolean = true): Discrete = sort match {
    case true => calcDiscrete(vals.sorted, nBins, false)
    case _ =>
      val min = vals.head
      val max = vals.last
      val binWidth = Math.max((max - min) / nBins, 1)
      val bins = vals.map(v => (((v - min)/ binWidth).toInt)).groupBy(identity).map{ case (k, v) => (k, v.size)}
      Discrete(
        vals.size,
        nBins,
        binWidth,
        mean(vals),
        vals(vals.size / 2),
        min,
        max,
        for ( i <- 0 to (nBins - 1))
          yield if (i < nBins - 1) bins.getOrElse(i, 0) else bins.getOrElse(i, 0) + bins.getOrElse(i + 1, 0)
      )
  }

  def calcShort(vals: Array[Short], nBins: Int = 30, sort: Boolean = true): Discrete = sort match {
    case true =>
      stableSort(vals)
      calcShort(vals, nBins, false)
    case _ =>
      val min = vals.head
      val max = vals.last
      val binWidth = Math.max((max - min) / nBins, 1)
      val bins = vals.map(v => (((v - min)/ binWidth).toInt)).groupBy(identity).map{ case (k, v) => (k, v.size)}
      Discrete(
        vals.size,
        nBins,
        binWidth,
        mean(vals),
        vals(vals.size / 2),
        min,
        max,
        for ( i <- 0 to (nBins - 1))
          yield if (i < nBins - 1) bins.getOrElse(i, 0) else bins.getOrElse(i, 0) + bins.getOrElse(i + 1, 0)
      )
  }

  def calcContinuous(vals: Seq[Float], nBins: Int = 30, sort: Boolean = true): Continuous = sort match {
    case true => calcContinuous(vals.sorted, nBins, false)
    case _ =>
      val min = vals.head
      val max = vals.last
      val binWidth = (max - min) / nBins
      val bins = vals.map(v => (((v - min)/binWidth).toInt)).groupBy(identity).map{ case (k, v) => (k, v.size)}
      Continuous(
        vals.size,
        nBins,
        (max - min) / nBins,
        mean(vals),
        vals(vals.size / 2),
        min,
        max,
        for ( i <- 0 to (nBins - 1))
          yield if (i < nBins - 1) bins.getOrElse(i, 0) else bins.getOrElse(i, 0) + bins.getOrElse(i + 1, 0)
      )
  }

  def calcFloat(vals: Array[Float], nBins: Int = 30, sort: Boolean = true): Continuous = sort match {
    case true =>
      stableSort(vals)
      calcFloat(vals, nBins, false)
    case _ =>
      val min = vals.head
      val max = vals.last
      val binWidth = (max - min) / nBins
      val bins = vals.map(v => (((v - min)/binWidth).toInt)).groupBy(identity).map{ case (k, v) => (k, v.size)}
      Continuous(
        vals.size,
        nBins,
        (max - min) / nBins,
        mean(vals),
        vals(vals.size / 2),
        min,
        max,
        for ( i <- 0 to (nBins - 1))
          yield if (i < nBins - 1) bins.getOrElse(i, 0) else bins.getOrElse(i, 0) + bins.getOrElse(i + 1, 0)
      )
  }
}
