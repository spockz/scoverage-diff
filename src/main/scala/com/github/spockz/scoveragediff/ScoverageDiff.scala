package com.github.spockz.scoveragediff

import java.io.File

import scoverage.{CoverageMetrics, MeasuredClass, MeasuredPackage}
import scoverage.report.ScoverageXmlReader

object ScoverageDiff extends App {

  val origin = args(0)
  val target = args(1)

  val originScoverage = ScoverageXmlReader.read(new File(origin))
  val targetScoverage = ScoverageXmlReader.read(new File(target))


  println(
    s"Global Coverage was **${originScoverage.statementCoverageFormatted}** and now is **${targetScoverage.statementCoverageFormatted}**."
  )


  def annotateMeasuredPackageName(annotation: String, measuredPackage: MeasuredPackage): MeasuredPackage =
    measuredPackage.copy(name = s"$annotation-${measuredPackage.name}")

  def annotateMeasuredClassName(annotation: String, measuredClass: MeasuredClass): MeasuredClass =
    measuredClass.copy(fullClassName = s"${measuredClass.fullClassName}-$annotation")


  val packageLevelDiff: Map[String, Seq[MeasuredPackage]] =
    Util.merge(
      originScoverage.packages.groupBy(_.name),
      targetScoverage.packages.groupBy(_.name).map(t => t.copy(_2 = t._2.map(annotateMeasuredPackageName("target", _))))
    )


  val reportTable = createTable[MeasuredPackage](packageLevelDiff, _.name.startsWith("target"))

  println()
  println("Overview of coverage changes per package: \n")
  println(Tabulator(Seq("package name", "previous coverage", "current coverage") +: reportTable))


  val classLevelDiff =
    Util.merge(
      originScoverage.classes.toSeq.groupBy(_.fullClassName),
      targetScoverage.classes.groupBy(_.fullClassName).map(t => t.copy(_2 = t._2.toSeq.map(annotateMeasuredClassName("target", _))))
    )


  val reportClassLevelTable = createTable[MeasuredClass](classLevelDiff, _.fullClassName.endsWith("target"))

  println()
  println("Overview of coverage changes per class: \n")
  println(Tabulator(Seq("class name", "previous coverage", "current coverage") +: reportClassLevelTable))


  val difference = targetScoverage.statementCoverage - originScoverage.statementCoverage
  if (difference < 0) {
    sys.exit(1)
  } else {
    sys.exit(0)
  }

  private[scoveragediff] def createTable[E <: CoverageMetrics](classLevelDiff: Map[String, Seq[E]], isTarget: E => Boolean): Seq[Seq[String]] = {
    classLevelDiff
      .collect {
        case (packageName, Seq(singlePackage)) =>
          if (isTarget(singlePackage)) {
            Seq(packageName, "0", singlePackage.statementCoverageFormatted)
          } else {
            Seq(packageName, singlePackage.statementCoverageFormatted, "0")
          }
        case (packageName, Seq(originPackage, targetPackage)) if originPackage.statementCoverage != targetPackage.statementCoverage =>
          Seq(packageName, originPackage.statementCoverageFormatted, targetPackage.statementCoverageFormatted)
      }
      .toSeq
      .sortBy(_.head)
  }
}

object Tabulator {
  def apply(table: Seq[Seq[String]]): StringBuilder = {
    val builder = new StringBuilder(1000)
    val transposed = table.transpose
    val maxLenghts = transposed.map(_.map(_.length).max)

    val printer = rowPrinter(builder, maxLenghts.toArray)(_)

    printer(table.head)
    printer(maxLenghts.map(n => "-" * n))

    table.tail.foreach(printer)

    //Remove trailing newline
    builder.deleteCharAt(builder.length - 1)

    builder
  }

  private def rowPrinter(stringBuilder: StringBuilder, columnWidth: Array[Int])(row: Seq[String]): Unit = {
    val length = row.length
    stringBuilder.append('|')
    row.zipWithIndex.foreach { case (column, i) =>
      stringBuilder.append(String.format(s" %1$$-${columnWidth(i)}s ", column))
      stringBuilder.append('|')
    }
    stringBuilder.append("\n")
  }

}

object Util {

  /**
    * Merge two maps where the values are sequences, concatenating the sequences when the key exists in both maps.
    *
    * @param left first map
    * @param right second map
    * @tparam K type of key in map
    * @tparam V type of the element in the sequence in the value of the map
    * @return a single map where the values of the sequences are concatenated for keys where they exist left and right
    */
  def merge[K, V](left: Map[K, Seq[V]], right: Map[K, Seq[V]]): Map[K, Seq[V]] =
    right.foldLeft(left) { case (acc: Map[K, Seq[V]], (k, vs: Seq[V])) =>
      acc.updated(k, acc.get(k).map(_ ++ vs).getOrElse(vs))
    }
}