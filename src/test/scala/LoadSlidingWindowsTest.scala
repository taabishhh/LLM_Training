import org.scalatest.funsuite.AnyFunSuite
import org.nd4j.linalg.dataset.DataSet

class LoadSlidingWindowsTest extends AnyFunSuite {
  test("loadSlidingWindowsFromCSV should load and parse DataSet from CSV file") {
    val testFilePath = "src/test/resources/sample_sliding_window_data.csv"
    val dataSets: List[DataSet] = TrainingWithSlidingWindowSpark.loadSlidingWindowsFromCSV(testFilePath)

    assert(dataSets.nonEmpty)
    assert(dataSets.head.getFeatures.length() > 0)
    assert(dataSets.head.getLabels.length() > 0)
  }
}