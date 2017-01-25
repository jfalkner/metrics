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

... 
[info] Statement coverage.: 100.00%
[info] Branch coverage....: 100.00%
[info] Coverage reports completed
[info] All done. Coverage was [100.00%]
```

See [TestMetrics in MetricsSpec.scala](src/test/scala/falkner/jayson/metrics/MetricsSpec.scala)
for an example that tests all of the features. Below is brief example
showing how data is serialized.

```scala
# Make a `Metrics` instance to have it be serialized
  class Example extends Metrics {
    override val namespace = "Example"
    override val version = "_"
    override lazy val values: List[Metric] = List(
      Str("Name", "Data Scientist"),
      Num("Age", "123"),
      DistCon("Data", calcContinuous(Seq(0f, 1f, 0.5f), nBins = 3, sort = true)),
      Num("Borken", throw new Exception("Calculation failed!"))
    )
  }

# Export as CSV for JMP, R, Excel, etc. Notice the error doesn't break the export.
# Also notice that the data is flat and omits histogram bins.
CSV(Paths.get("example.csv", new Example())

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

A more advanced use of this API is to expose a `View`, which is exporting tabular data (CSV) with any arbitrary subset
of values, in any column order and with optional custom naming for the columns. This is helpful since most of the usage 
of this API is to expose tabular exports for Excel, JMP, R, Tableau, Spotfire and ilk.

```scala
class ExampleView extends View {
  override lazy val name = "Example View"
  override lazy val description = "An example custom tabular export for the metrics API documentation."
  // show just three columns, "name", "mean" and "median, and force lowercase names -- for whatever reason that is preferred
  override lazy val metrics = List[Col] {
    Col("name", "Example", "Name"),
    Col("mean", "Example", "Data", Some("Mean")),
    Col("median", "Example", "Data", Some("Median")),
  }
}
```

Export of a view is usually handled by the database. See [`Cache.queriesToCsv`](https://github.com/jfalkner/metrics-cache/blob/d484ae86394f04ab4d5187d97b168e97b6f986d6/src/main/scala/falkner/jayson/metrics/cache/Cache.scala#L35) for an example.


## Suggested Versioning Conventions

These are helpful conventions to follow for supporting common use cases. These examples assume you are using
[semantic versioning](http://semver.org/), but any version string can be used in a similar fashion.

### Use 'Code Version' and 'Spec Version"

Make metrics modules that have a `build.sbt` version that is mirrored as the "Code Version" in a companion object, and
also have a "Spec Version" that represents the version of the underlying file format you are parsing. Together these
give a way to later sort data based on if the metrics code was updates and/or if the underlying data format changed.

An example of this can be seen in `MetricsWithVersion` and `MetricsWithVersion_1_2_3` [here](https://github.com/jfalkner/metrics-examples/blob/e7a8a22acac87fc2b66b3cbfd01dd2e7fae20ae1/src/main/scala/falkner/jayson/metrics/example/MetricWithVersions.scala#L44-L45) in the `metrics-examples` repo.

### Use and object with `apply` to encapsulate version parsing logic

The main strategy is as simple as capturing the version detecting logic in an `apply` method of an object, which serves
as the entry point to parsing data. An example is in `MetricsWithVersion` [here](https://github.com/jfalkner/metrics-examples/blob/5f769f9fc46ed3234569b5a2ae572b994eeb4a6b/src/main/scala/falkner/jayson/metrics/example/MetricWithVersions.scala#L29-L37) in the `metrics-example` repo.

### `blank` for making CSV headers

Having a val named `blank` that is a `null` or otherwise no-arg created instance of the current `Metrics` is required 
for making headers in CSV exports, namely `View` instances. With a `blank` you can easily make a view that is just a
copy of all values.

An example of `blank` is in `MetricsWithVersion` [here](https://github.com/jfalkner/metrics-examples/blob/618210b4d9ad5dc1ec9798c3653f67e02d64dd8e/src/main/scala/falkner/jayson/metrics/example/MetricWithVersions.scala#L29) in the `metrics-example` repo.