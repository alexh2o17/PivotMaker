package com.h2o.PivotMaker.model

import com.h2o.PivotMaker.util.ErrorHandler

import scala.annotation.tailrec

case class PivotNode (value:String,result: Int, nodes: Map[String,PivotNode]) {
    val isLastLevel :Boolean = nodes.isEmpty
}


case class PivotHeaderIndex(name: String, index:Int)

case class PivotHeader(headers: List[PivotHeaderIndex],resultIndex: PivotHeaderIndex) {
  def getAggregationOrderName : List[String] = headers.map(_.name)
  def getAllNamesWithResult : List[String] = getAggregationOrderName ++ List("Result")
  def getAllIndices: List[Int] = headers.map(_.index)
}

object PivotHeader extends ErrorHandler{

  def getHeader(headerRow: List[String],aggregationOrder: List[String], resultIndex: String)  : Either[Throwable, PivotHeader] ={
    headerRow match {
      case Nil => Left(leftError("No header Defined"))
      case _ =>
        for{
          _ <- if(aggregationOrder.contains(resultIndex)) Left(leftError("Result column could not be one of the aggregation columns")) else Right()
          _ <- checkSize(headerRow,aggregationOrder)
          indexedHeaderRow = headerRow.map(_.toUpperCase).zipWithIndex.toMap
          result <- indexedHeaderRow.get(resultIndex.toUpperCase).toRight(leftError(s"Result column \'$resultIndex\' not Exist")).map(PivotHeaderIndex(resultIndex,_))
          _ <- if(indexedHeaderRow.size.equals(headerRow.size)) Right() else Left(leftError("Impossible to define Pivot Table: Columns with same name is not accepted"))
          pivotHeader <- PivotHeader.getFromEitherList(aggregationOrder.map(agg => indexedHeaderRow.get(agg.toUpperCase).map(PivotHeaderIndex(agg,_)).toRight(s"$agg not exist")))
        } yield PivotHeader(pivotHeader,result)
    }
  }

  private def checkSize(headerRow: List[String],aggregationOrder: List[String]) : Either[Throwable, Unit] = if(aggregationOrder.size > headerRow.size) Left(leftError("Impossible to define Pivot Table: Too Much aggregation field")) else Right()

  private def getFromEitherList(rows: List[Either[String,PivotHeaderIndex]]) : Either[Throwable,List[PivotHeaderIndex]] ={
    val (partialLeft, partialRight) = rows.partition(_.isLeft)
    if(partialLeft.nonEmpty) Left(leftError(partialLeft.map(_.left.get).mkString("\n"))) else Right(partialRight.map(x => x.right.get))
  }
}

case class PivotTable (pivotHeader: PivotHeader,nodes: Map[String,PivotNode]) {

  def getResult(value: String*) : Either[Throwable,Map[String,Int]] = {
    if(value.size > pivotHeader.getAggregationOrderName.size) Left(new RuntimeException("Too many argument for these request")) else recursiveResult(value,Right(PivotNode("",0,nodes)))
  }

  def getResultByIndex(indexName: String) : Either[Throwable,Map[String,PivotNode]] = {
    for{
    headerIndex <- pivotHeader.headers.find(x => x.name.equalsIgnoreCase(indexName)).toRight(new RuntimeException("Index not found in Pivot Table"))
    } yield recursiveResultByIndex(headerIndex.index,nodes)
  }

  protected def getSingleResult(internalNodes: Map[String,PivotNode]) : Map[String,Int]  = internalNodes.map(node => node._1 -> node._2.result)

  @tailrec
  final protected def recursiveResult(value: Seq[String], nodes: Either[Throwable,PivotNode] ) : Either[Throwable,Map[String,Int]] = {
    if(value.isEmpty || nodes.isLeft) {
      nodes match {
        case Left(er) => Left(er)
        case Right(value) =>Right(if(value.isLastLevel) Map(value.value -> value.result) else getSingleResult(value.nodes))
      }
    } else{
      recursiveResult(value.tail,nodes.right.get.nodes.get(value.head).toRight(new RuntimeException("No results founded with these condition")))
    }
  }

  final protected def recursiveResultByIndex(totalLevel: Int, allNodes: Map[String,PivotNode], actualLevel: Int = 0) : Map[String,PivotNode] = {
    if(actualLevel.equals(totalLevel)) {
      allNodes.map(x => x._1 -> x._2.copy(nodes = Map.empty))
    } else{
      allNodes.map(x => x._1 -> x._2.copy(nodes = recursiveResultByIndex(totalLevel,x._2.nodes,actualLevel + 1)))
    }
  }


}

object PivotTable extends ErrorHandler {

  def create(data: List[List[String]], aggregationFunction: List[Int] => Int, aggregationOrder: List[String], resultIndex: String) : Either[Throwable,PivotTable] = {
    for {
      table <- checkSize(data)
      pivotHeader <- PivotHeader.getHeader(table._1,aggregationOrder,resultIndex)
      filteredRows = table._2.map(FilteredRow.create(_,pivotHeader.getAllIndices,pivotHeader.resultIndex.index)).collect{case Right(value) => value}
      finalData <- try{Right(createNodes(filteredRows,aggregationFunction))} catch {case e: Throwable => Left(e)}
    } yield PivotTable(pivotHeader,finalData)
  }


  private def createNodes(rows: List[FilteredRow], aggregationFunction: List[Int] => Int,end:Boolean = false): Map[String,PivotNode] ={
    if(end) {
      Map.empty[String,PivotNode]
    } else{
      rows.groupBy(_.values.head).map{case (name, grouped) =>
        val (filtered, results) =grouped.unzip(x => x.copy(x.values.tail) -> x.result)
        name -> PivotNode(name,aggregationFunction(results),createNodes(filtered,aggregationFunction,filtered.exists(x => x.values.isEmpty)))}
    }

  }
  private def checkSize(data: List[List[String]]): Either[Throwable, (List[String], List[List[String]])] =
    data match {
      case ::(head, tl) => if (tl.nonEmpty) Right(head -> tl) else Left(leftError("No Data defined in Origin Table, only header founded"))
      case Nil => Left(leftError("Origin Table is empty"))
    }


}