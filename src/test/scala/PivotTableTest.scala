import com.h2o.PivotMaker.model.PivotHeader
import org.scalatest._
import flatspec._
import matchers._


class PivotTableTest extends AnyFlatSpec  with should.Matchers  {

  "A pivot Header" should "contains same column of Aggregation order" in{
    for{
      header <- PivotHeader.getHeader(List("Nation","Eyes", "Hair"), List("Eyes","Nation", "Hair"))
      headerName = header.getAggregationOrderName
      headerIndices = header.getAllIndices
    } yield{
      headerName should be (List("Eyes","Nation", "Hair"))
      headerIndices should be (List(1,0,2))
    }
  }

  it should "fail if Aggregation order list is > than total column" in {
    val pivotHeader = PivotHeader.getHeader(List("Nation"), List("Eyes","Nation", "Hair"))
    pivotHeader.isRight should be (false)
  }

  it should "fail if column are not unique" in {
    val pivotHeader = PivotHeader.getHeader(List("Nation","Eyes", "Eyes","Hair"), List("Eyes","Nation", "Hair"))
    pivotHeader.isRight should be (false)
  }

  it should "fail if an aggregation column not exist " in {
    val pivotHeader = PivotHeader.getHeader(List("Nation","Eyes","Hair"), List("Height"))
    pivotHeader.isRight should be (false)
    pivotHeader.left.get.getMessage should be ("Height not exist")
  }
}
