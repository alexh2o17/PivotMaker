import com.h2o.PivotMaker.model.{PivotHeader, PivotNode, PivotTable}
import org.scalatest._
import flatspec._
import matchers._


class PivotTableTest extends AnyFlatSpec  with should.Matchers  {

  val data = SpecUtils.readCSV("test.csv",';')
  def sum(ls: List[Int]) : Int = ls.sum

  "A pivot Header" should "contains same column of Aggregation order" in{
    for{
      header <- PivotHeader.getHeader(List("Nation","Eyes", "Hair","Result"), List("Eyes","Nation", "Hair"), "Result")
      headerName = header.getAggregationOrderName
      headerIndices = header.getAllIndices
    } yield{
      headerName should be (List("Eyes","Nation", "Hair"))
      headerIndices should be (List(1,0,2))
    }
  }

  it should "fail if Aggregation order list is > than total column" in {
    val pivotHeader = PivotHeader.getHeader(List("Nation"), List("Eyes","Nation", "Hair"),"Result")
    pivotHeader.isRight should be (false)
    pivotHeader.left.get.getMessage should be ("Impossible to define Pivot Table: Too Much aggregation field")
  }

  it should "fail if column are not unique" in {
    val pivotHeader = PivotHeader.getHeader(List("Nation","Eyes", "Eyes","Hair","Result"), List("Eyes","Nation", "Hair"),"Result")
    pivotHeader.isRight should be (false)
    pivotHeader.left.get.getMessage should be ("Impossible to define Pivot Table: Columns with same name is not accepted")

  }

  it should "fail if an aggregation column not exist " in {
    val pivotHeader = PivotHeader.getHeader(List("Nation","Eyes","Hair","Result"), List("Height"),"Result")
    pivotHeader.isRight should be (false)
    pivotHeader.left.get.getMessage should be ("Height not exist")
  }
  it should "fail if result column not exist" in {
    val pivotHeader = PivotHeader.getHeader(List("Nation","Eyes","Hair"), List("Nation"),"Result")
    pivotHeader.isRight should be (false)
    pivotHeader.left.get.getMessage should be ("Result column \'Result\' not Exist")
  }
  it should "fail if result column is one of the aggregation field" in {
    val pivotHeader = PivotHeader.getHeader(List("Nation","Eyes","Hair"), List("Nation"),"Nation")
    pivotHeader.isRight should be (false)
    pivotHeader.left.get.getMessage should be ("Result column could not be one of the aggregation columns")
  }
  it should "fail if header is empty" in {
    val pivotHeader = PivotHeader.getHeader(Nil, List("Nation"),"Nation")
    pivotHeader.isRight should be (false)
    pivotHeader.left.get.getMessage should be ("No header Defined")
  }
  "A pivot Table" should "be created right" in{
    val pivotTable  = PivotTable.create(data,sum,List("Nations","Eyes","Hair"),"Total")
    println(pivotTable)
    pivotTable.isRight should be (true)
  }
  it should "fail with invalid Aggregation order" in {
    val pivotTable  = PivotTable.create(data,sum,List("Nation","Eyes","Hair"),"Total")
    pivotTable.isRight should be (false)
  }
  it should "fail with invalid Result Index" in {
    val pivotTable  = PivotTable.create(data,sum,List("Nations","Eyes","Hair"),"Total1")
    pivotTable.isRight should be (false)
  }
  it should "get results at first level" in {
    val pivotTable  = PivotTable.create(data,sum,List("Nations","Eyes","Hair"),"Total")
    pivotTable.isRight should be (true)
    val realPivot = pivotTable.right.get
    val res = realPivot.getResultByValue()
    res.isRight should be (true)
    val realRes  = res.right.get
    realRes.size should be (4)
    realRes.head should be (("Italy",148))
    realRes.tail.head should be (("France",2149))
    realRes.tail.tail.head should be (("Germany",3323))
    realRes.last should be (("Spain",2896))
  }
  it should "create the right sum of results" in {
    val pivotTable  = PivotTable.create(data,sum,List("Nations","Eyes","Hair"),"Total")
    pivotTable.isRight should be (true)
    val realPivot = pivotTable.right.get
    val res = realPivot.getResultByValue("France","Green")
    res.isRight should be (true)
    val realRes  = res.right.get
    realRes.head._2 should be (288)
    realRes.last._2 should be (857)
  }

  it should "return only on element if you query on all condition" in {
    val pivotTable  = PivotTable.create(data,sum,List("Nations","Eyes","Hair"),"Total")
    pivotTable.isRight should be (true)
    val realPivot = pivotTable.right.get
    val res = realPivot.getResultByValue("France","Green","Black")
    res.isRight should be (true)
    val realRes  = res.right.get
    realRes.size should be (1)
    println(realRes)
    realRes.head._1 should be ("Black")
    realRes.head._2 should be (857)
  }
  it should "fail if without data" in {
    val pivotTable  = PivotTable.create(Nil,sum,List("Nations","Eyes","Hair"),"Total")
    pivotTable.isRight should be (false)
    pivotTable.left.get.getMessage should be ("Origin Table is empty")
  }

  it should "fail if we use too conditions" in {
    val pivotTable  = PivotTable.create(data,sum,List("Nations","Eyes","Hair"),"Total")
    pivotTable.isRight should be (true)
    val realPivot = pivotTable.right.get
    val res = realPivot.getResultByValue("France","Green","Black","Black")
    res.isRight should be (false)
  }
  it should "get only first level on Nations query" in {
    val pivotTable  = PivotTable.create(data,sum,List("Nations","Eyes","Hair"),"Total")
    pivotTable.isRight should be (true)
    val realPivot = pivotTable.right.get
    val res = realPivot.getResultByIndex("Nations")
    res.isRight should be (true)
    val realRes  = res.right.get
    realRes.size should be (4)
    realRes.head should be (("Italy",PivotNode("Italy",148,Map())) )
    realRes.tail.head should be ("France",PivotNode("France",2149,Map()))
    realRes.tail.tail.head should be ("Germany",PivotNode("Germany",3323,Map()))
    realRes.last should be ("Spain",PivotNode("Spain",2896,Map()))
  }
  it should "get two level on Nations query" in {
    val pivotTable  = PivotTable.create(data,sum,List("Nations","Eyes","Hair"),"Total")
    pivotTable.isRight should be (true)
    val realPivot = pivotTable.right.get
    val res = realPivot.getResultByIndex("Eyes")
    res.isRight should be (true)
    val realRes  = res.right.get
    realRes.size should be (4)
    realRes.head._2.nodes.size should be (1)
    realRes.tail.head._2.nodes.size should be (2)
    realRes.tail.tail.head._2.nodes.size should be (4)
    realRes.last._2.nodes.size should be (4)
  }
  it should "fail with wrong aggregation field" in {
    val pivotTable  = PivotTable.create(data,sum,List("Nations","Eyes","Hair"),"Total")
    pivotTable.isRight should be (true)
    val realPivot = pivotTable.right.get
    val res = realPivot.getResultByIndex("EyesError")
    res.isRight should be (false)
  }
}
