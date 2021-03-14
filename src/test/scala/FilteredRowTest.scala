import com.h2o.PivotMaker.model.FilteredRow
import org.scalatest._
import flatspec._
import matchers._

class FilteredRowTest extends AnyFlatSpec  with should.Matchers {

  "A Filtered Row" should "contains same column of Aggregation order" in {
    for {
      filteredRow <- FilteredRow.create(List("Spain", "Dark", "Black", "907"), List(0, 1, 2), 3)
    } yield {
      filteredRow.result should be (907)
      filteredRow.values.head should be ("Spain")
    }
  }

  it should "fail if result value is not a number" in {
      val filteredRow = FilteredRow.create(List("Spain", "Dark", "Black", "907"), List(0, 1, 3), 2)
      filteredRow.isRight should be (false)
  }

  it should "fail if column not exist" in {
    val filteredRow = FilteredRow.create(List("Spain", "Dark", "Black", "907"), List(0, 1, 4), 3)
    filteredRow.isRight should be (false)
  }

}
