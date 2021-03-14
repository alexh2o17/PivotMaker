//package com.h2o.PivotMaker.impl
//
//import com.h2o.PivotMaker.model.{FilteredRow, PivotTable}
//
//object maker {
//
//  def createPivot(data: List[List[String]], aggregationFunction: List[Int] => Int, aggregationOrder: List[String]): Either[Throwable,PivotTable] =
//    for {
//    filteredRow <- data.tail.map(row => FilteredRow.create(row,))
//
//  } yield result
//
//  private def getAggregateIndex(header: List[String], aggregationOrder: List[String]) ={
//    header.zipWithIndex()
//    aggregationOrder.map(index => )
//
//
//  }
//
//}
