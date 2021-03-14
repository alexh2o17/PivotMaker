package com.h2o.PivotMaker.model

import com.h2o.PivotMaker.model.PivotTable.leftError
import com.h2o.PivotMaker.util.ErrorHandler

case class PivotNode (value:String,result: Int, nodes: Map[String,PivotNode])

case class PivotHeaderIndex(name: String, index:Int)

case class PivotHeader(headers: List[PivotHeaderIndex]) {
  def getAggregationOrderName : List[String] = headers.map(_.name)
  def getAllNamesWithResult : List[String] = getAggregationOrderName ++ List("Result")
  def getAllIndices: List[Int] = headers.map(_.index)
}

object PivotHeader extends ErrorHandler{

  def getHeader(headerRow: List[String],aggregationOrder: List[String])  : Either[Throwable, PivotHeader] ={
    headerRow match {
      case ::(head, tl) =>
        for{
          _ <- checkSize(headerRow,aggregationOrder)
          indexedHeaderRow = headerRow.zipWithIndex.toMap
          _ <- if(indexedHeaderRow.size.equals(headerRow.size)) Right() else Left(leftError("Impossible to define Pivot Table: Column with same name is not accepted"))
          pivotHeader <- PivotHeader.getFromEitherList(aggregationOrder.map(agg => indexedHeaderRow.get(agg).map(PivotHeaderIndex(agg,_)).toRight(s"$agg not exist")))
        } yield PivotHeader(pivotHeader)
      case Nil => Left(leftError("No header Defined"))
    }
  }

  private def checkSize(headerRow: List[String],aggregationOrder: List[String]) : Either[Throwable, Unit] = if(aggregationOrder.size > headerRow.size) Left(leftError("Impossible to define Pivot Table: Too Much aggregation field")) else Right()

  private def getFromEitherList(rows: List[Either[String,PivotHeaderIndex]]) : Either[Throwable,List[PivotHeaderIndex]] ={
    val (partialLeft, partialRight) = rows.partition(_.isLeft)
    if(partialLeft.nonEmpty) Left(leftError(partialLeft.map(_.left.get).mkString("\n"))) else Right(partialRight.map(x => x.right.get))
  }
}

case class PivotTable (aggregationOrder: List[String],nodes: Map[String,PivotNode])

object PivotTable extends ErrorHandler{



}