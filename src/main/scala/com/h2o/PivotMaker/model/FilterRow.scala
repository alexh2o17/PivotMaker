package com.h2o.PivotMaker.model

import com.h2o.PivotMaker.util.ErrorHandler

case class FilteredRow (values: List[String],result:Int)

object FilteredRow extends ErrorHandler {

  def create(row: List[String], orderIndex: List[Int],resultIndex: Int) : Either[Throwable,FilteredRow] =
    for {
    resIndex <- row.lift(resultIndex).toRight(leftError("Result value not found"))
    result <-  getResult(resIndex)
    //Column are ordered by aggregation order
    values <- getValues(orderIndex.map(index => row.lift(index)))
    } yield FilteredRow(values,result)

  protected def getResult(data: String) : Either[Throwable, Int] = {
    try{
      Right(data.toInt)
    }catch {
      case er: Throwable => Left(er)
    }
  }
  protected def getValues(data: List[Option[String]]) : Either[Throwable, List[String]] = {
    val partialRight = data.collect{ case Some(value) =>  value}
    if(!partialRight.size.equals(data.size)) Left(leftError("Some columns not found")) else Right(partialRight)
  }

}