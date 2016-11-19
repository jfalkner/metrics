package falkner.jayson.metrics

import java.nio.file.{Files, Path}

import falkner.jayson.metrics.io.CSV
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

import scala.collection.JavaConverters._


/**
  * Confirms that custom number formatting works
  *
  * Use cases:
  *
  * 1. Default format floats to 2 decimal places. Saves space in serialized output
  * 2. Confirm that String value Nums never are formatted -- also allows for custom formatting
  */
class NumFormatSpec extends Specification {

  "Num formatting" should {
    "Default Double to 2 decimal places" in (Num("Test", Math.PI).value mustEqual "3.14")
    "Default Float to 2 decimal places" in (Num("Test", Math.PI.toFloat).value mustEqual "3.14")
    "Don't pad Float to 2 decimal places" in (Num("Test", 1.5f).value mustEqual "1.5")
    "Don't pad Decimal to 2 decimal places" in (Num("Test", 1.5d).value mustEqual "1.5")
    "Keep string precision as-is" in (Num("Test", "0.123456").value mustEqual "0.123456")

  }
}



