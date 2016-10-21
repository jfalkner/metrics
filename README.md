# Metrics

If you are generating metrics and deriving analyses and reports, this is
a framework that formalizes most of the common use cases. It comes from
years of working as a bioinformatician and having to repetitively
formalize data exports and reporting.

In most previous gigs, analytics have been various degress of a mess.
This framework formalizes what is important so that you don't have a mess,
but rather an extensible metrics export so you can focus on what the data
means. 

## Key Features

- Data exports to CSV and JSON. Most commonly used formats.
  * CSV exports are "flat", meaning simple to use in Excel, JMP, R, etc.
  * JSON exports have the full graph of data, such as values for histogram bins.
- Try to return the exact String (aka TryExact[String]).
   * If calculation fails, don't blow up. 
   * Numbers parsed from a text file (XML, CSV, etc) should end up as the exact same string when exported. Don't do a lossly convert (e.g. to float) then serialize.

## Usage

This is an API and doesn't do anything on its own other that provide tests and coverage.

```
# Build a JAR
sbt clean coverage test coverageReport
```

## Examples

See the test cases for examples. TODO: link to some projects showing
further use.