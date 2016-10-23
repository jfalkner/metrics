# Metrics

A simple, Scala-based framework for extracting data and exporting it to JSON
and/or CSV. Convenient for making data that Web/JS visualizations use or
for users relying on tools such as JMP, R, Excel, etc.

## Key Features

- Data exports to CSV and JSON
  * CSV exports are "flat", meaning simple and succinct. Intended for Excel, JMP, R, and ilk
  * JSON exports have the full graph of data, such as values for histogram bins
- Errors don't cause exports to blow up nor will nonsensical default values be exported (e.g. 0 or -1 for a number)
  * CSV cells show a blank if a value couldn't be calculated
  * JSON exports omit failed values. Pushes error handling to JS or viz tool
- Convenience methods for writing succinct code without any data serialization boiler plate required

## Usage

This is an API. It doesn't run on its own other than to provide tests and coverage.

```
# Build a JAR
sbt clean coverage test coverageReport
```

See [TestMetrics in MetricsSpec.scala](src/test/scala/falkner/jayson/metrics/MetricsSpec.scala)
for an example that tests all of the features. Below is brief example
showing how data is serialized.

```
# Make a `Metrics` instance to have it be serialized
class Example extends Metrics {
  override lazy val values: List[Metric] = List(
    Str("Name", calcName),
    Num("Age", calcAge),
    Dist("Data", calcContinuousDist(Seq(0f, 1f, 0.5f), nBins = 3, sort = true)),
    Num("Borken", willThrowError)
  )
    
  val willThrowError = () => throw new Exception("Calculation failed!")
  val calcName = () => "Data Scientist"
  val calcAge = () => "21"
}

# Export as CSV for JMP, R, Excel, etc. Notice the error doesn't break the export.
# Also notice that the data is flat and omits histogram bins.
CSV.export(Paths.get("example.csv", new Example())

# example.csv 
Name,Age,Data: Samples,Data: Bins,Data: BinWidth,Data: Mean,Data: Median,Data: Min,Data: Max,Borken
Data Scientist,21,3,3,0.33333334,0.5,0.5,0.0,1.0,

# Export as CSV for fully serialized data, convenient for Web/JS or data viz tools.
JSON.export("example.json", new Example())

# example.json
{
  "Name": "Data Scientist",
  "Age": 21,
  "Data": {
    "Min": 0.0,
    "Mean": 0.5,
    "Max": 1.0,
    "Bins": [1, 1, 1],
    "BinWidth": 0.33333334,
    "Samples": 3,
    "Median": 0.5
  }
}
```
