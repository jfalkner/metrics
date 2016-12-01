package falkner.jayson.metrics

import scala.collection.mutable
import scala.util.Sorting.stableSort


/**
  * Summarizes sets of values as distributions
  *
  * Helpful for exporting histogram
  */
object Distribution {

  case class Continuous(sampleNum: Long, binNum: Int, binWidth: Float, mean: Float, median: Float, min: Float, max: Float, bins: Seq[Int])

  case class Discrete(sampleNum: Long, binNum: Int, binWidth: Int, mean: Float, median: Int, min: Int, max: Int, bins: Seq[Int])

  case class Categorical(sampleNum: Long, bins: Map[String, AnyVal])

  def makeCategorical(vals: Map[String, Int]): Categorical = Categorical(vals.values.sum, vals)

  def calcCategorical(vals: Map[String, Traversable[Any]]): Categorical =
    Categorical(vals.values.map(_.size).sum, vals.map{ case (k, v) => (k, v.size) })

  def mean(vals: Seq[AnyVal], size: Long): Float = mean(vals, Some(size))

  def mean(vals: Seq[AnyVal], size: Option[Long] = None): Float = {
    var bd = BigDecimal(0)
    vals.foreach(_ match {
      case v: Long => bd += v
      case v: Int => bd += v
      case v: Short => bd += v.toInt
      case v: Float => bd += v.toDouble
      case v: Double => bd += v
    })
    size match {
      case Some(d) => (bd / d).toFloat
      case None => if (!vals.isEmpty) (bd / vals.size).toFloat else bd.toFloat
    }
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
  def calcShort(vals: Seq[Short], nBins: Int, sort: Boolean, forceMin: Int, forceMax: Int): Discrete =
    calcShort(vals, nBins, sort, Some(forceMin), Some(forceMax))

  def calcShort(vals: Seq[Short], nBins: Int = 30, sort: Boolean = true, forceMin: Option[Int] = None, forceMax: Option[Int] = None): Discrete = sort match {
    case true =>
      stableSort(vals)
      calcShort(vals, nBins, false)
    case _ =>
      val min = forceMin match {
        case Some(v) => v
        case None => if (vals.size > 0) vals.head else 0
      }
      val max = forceMax match {
        case Some(v) => v
        case None => if (vals.size > 0) vals.last else 0
      }
      //val min = if (vals.size > 0) vals.head else 0
      //val max = if (vals.size > 0) vals.last else 0
      val binWidth = Math.max((max - min) / nBins, 1)
      val bins = vals.map(v => (((v - min)/ binWidth).toInt)).groupBy(identity).map{ case (k, v) => (k, v.size)}
      Discrete(
        vals.size,
        nBins,
        binWidth,
        mean(vals),
        if (!vals.isEmpty) vals(vals.size / 2) else 0,
        min,
        max,
        for ( i <- 0 to (nBins - 1))
          yield if (i < nBins - 1) bins.getOrElse(i, 0) else bins.getOrElse(i, 0) + bins.getOrElse(i + 1, 0)
      )
  }

  def calcContinuous(vals: Seq[Float], nBins: Int, sort: Boolean, forceMin: Float, forceMax: Float): Continuous =
    calcContinuous(vals, nBins, sort, Some(forceMin), Some(forceMax))

  def calcContinuous(vals: Seq[Float], nBins: Int = 30, sort: Boolean = true, forceMin: Option[Float] = None, forceMax: Option[Float] = None): Continuous = sort match {
    case true => calcContinuous(vals.sorted, nBins, false)
    case _ =>
      val min = forceMin match {
        case Some(v) => v
        case None => vals.head
      }
      val max = forceMax match {
        case Some(v) => v
        case None => vals.last
      }
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

  def mergeDiscrete(dists: Seq[Discrete], nBins: Int, forceMin: Int, forceMax: Int): Discrete =
    mergeDiscrete(dists, nBins, Some(forceMin), Some(forceMax))

  def mergeDiscrete(dists: Seq[Discrete], nBins: Int = 30, forceMin: Option[Int] = None, forceMax: Option[Int] = None): Discrete = {
    if (dists.isEmpty) Discrete(0, 0, 0, 0, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, Nil)
    else {
      val min = forceMin match {
        case Some(v) => v
        case None => dists.map(_.min).min
      }
      val max = forceMax match {
        case Some(v) => v
        case None => dists.map(_.max).max
      }
      val binWidth = Math.max((max - min) / nBins, 1)
      // make one array to populate
      val a = new Array[Int](nBins)
      // convert all dists from (binIndex, count) => (value, count). So all values across all dist are in the same units
      dists.foreach(
        d => d.bins.zipWithIndex
          .map { case (v, i) => (d.min + (i * d.binWidth), v) }
          //.foreach { case (v, count) => a(Math.min(nBins -1, ((v - min) / binWidth))) += count }
          .foreach { case (v, count) => a(Math.max(0, Math.min(nBins -1, ((v - min) / binWidth)))) += count }
      )
      val samples = a.map(_.toLong).sum
      val vals = a.zipWithIndex.map { case (v, i) => (min + (i * binWidth)) * v }
      val meanV = mean(vals, samples)
      val median = if (!dists.isEmpty) (dists.map(_.median).sorted[Int].toList)(dists.size / 2) else 0
      Discrete(samples, nBins, binWidth, meanV, median, min, max, a)
//      // reset bounds to show 99% of samples?
//      var tally = 0
//      var maxBin = 0
//      for (i <- 0 to (nBins-1) if tally < samples * 99.9 / 100) {
//        tally += a(i)
//        maxBin = i + 1
//      }
//      // remake with
//      val min99 = min
//      val max99 = min + maxBin * binWidth
//      val binWidth99 = Math.max((max99 - min99) / nBins, 1)
//      // make one array to populate
//      val a99 = new Array[Int](nBins)
//      // convert all dists from (binIndex, count) => (value, count). So all values across all dist are in the same units
//      dists.foreach(
//        d => d.bins.zipWithIndex
//          .map { case (v, i) => (d.min + (i * d.binWidth), v) }
//          .filter(_._1 < max99) // throw away the long tail
//          .foreach { case (v, count) => a99(Math.min(nBins -1, ((v - min99) / binWidth99))) += count }
//      )
//      val samples99 = a99.sum
//
//      val vals = a99.zipWithIndex.map { case (v, i) => (min99 + (i * binWidth99)) * v }
//      val meanV = mean(vals)
//      val median = if (!vals.isEmpty) vals(vals.size / 2) else 0
//      Discrete(samples99, nBins, binWidth99, meanV, median, min99, max99, a99)
    }
  }

  def mergeContinuous(dists: Seq[Continuous], nBins: Int, forceMin: Float, forceMax: Float): Continuous =
    mergeContinuous(dists, nBins, Some(forceMin), Some(forceMax))

  def mergeContinuous(dists: Seq[Continuous], nBins: Int = 30, forceMin: Option[Float]=None, forceMax:Option[Float]=None): Continuous = {
    if (dists.isEmpty) Continuous(0, 0, 0, 0, 0, Float.MaxValue, Float.MinValue, Nil)
    else {
      val min = forceMin match {
        case Some(v) => v
        case None => dists.map(_.min).min
      }
      val max = forceMax match {
        case Some(v) => v
        case None => dists.map(_.max).max
      }
      val binWidth = if (max - min > 0) (max - min) / nBins else 1
      // make one array to populate
      val a = new Array[Int](nBins)
      // convert all dists from (binIndex, count) => (value, count). So all values across all dist are in the same units
      dists.foreach(
        d => d.bins.zipWithIndex
          .map { case (v, i) => (d.min + (i * d.binWidth), v) }
          .foreach { case (v, count) => a(Math.max(0, Math.min(nBins -1, ((v - min) / binWidth).toInt))) += count }
      )
      val samples = a.map(_.toLong).sum
      // reset bounds to show 99% of samples?
      val vals = a.zipWithIndex.map { case (v, i) => (min + (i * binWidth)) * v }
      val meanV = mean(vals, samples)
      val median = if (!dists.isEmpty) (dists.map(_.median).sorted[Float].toList)(dists.size / 2) else 0f
      Continuous(samples, nBins, binWidth, meanV, median, min, max, a)
    }
  }

  def mergeCategorical(dists: Seq[Categorical]): Map[String, Int] = {
    val m = mutable.ListMap[String, Int]()
    dists.foreach(d => d.bins.foreach{ case (k, v) => m.update(k, m.getOrElse(k,  0) + (v match { case i: Int => i })) })
    m.toMap
  }
}
