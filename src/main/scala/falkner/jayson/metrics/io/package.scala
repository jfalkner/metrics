package falkner.jayson.metrics

import java.nio.file.{Files, Path}


package object io {
  def mkdir(out: Path): Path = Files.createDirectories(out.getParent).resolve(out.getFileName)
}