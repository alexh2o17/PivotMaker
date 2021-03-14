package com.h2o.PivotMaker.util

import com.typesafe.scalalogging.LazyLogging

trait ErrorHandler extends LazyLogging{

  def leftError(message:String) = new RuntimeException(message)

}
