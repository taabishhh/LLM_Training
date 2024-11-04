import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame

class MainAppTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  var spark: SparkSession = _

  override def beforeAll(): Unit = {
    spark = SparkSession.builder()
      .appName("MainAppTest")
      .master("local[*]")
      .getOrCreate()
  }

  "MainApp" should "create a DataFrame from CSV data" in {
    val spark = SparkSession.builder()
      .appName("MainAppTest")
      .master("local[*]")
      .getOrCreate()

    // Define the sample data and schema
    val sampleData = Seq(
      ("2021-01-01 00:00:00", "station1", 40.7128, -74.0060, "member"),
      ("2021-01-02 01:00:00", "station2", 34.0522, -118.2437, "casual")
    )

    val columns = Seq("ride_start_time", "station_name", "latitude", "longitude", "rider_type")

    // Convert sample data to DataFrame
    val df = spark.createDataFrame(sampleData).toDF(columns: _*)

    // Test the DataFrame creation
    df.columns should contain theSameElementsAs columns
    df.count() shouldEqual 2
  }

  it should "handle empty DataFrames correctly" in {
    val spark = SparkSession.builder()
      .appName("MainAppTest")
      .master("local[*]")
      .getOrCreate()

    // Create an empty DataFrame
    val emptyDF = spark.emptyDataFrame

    // Check that the DataFrame is empty
    emptyDF.count() shouldEqual 0
    emptyDF.columns shouldBe empty
  }

  it should "handle invalid CSV data gracefully" in {
    val spark = SparkSession.builder()
      .appName("MainAppTest")
      .master("local[*]")
      .getOrCreate()

    // Define a path to a non-existent CSV file
    val invalidPath = "token_embeddings2.csv"

    // Try to read the CSV file and expect an exception
    val exception = intercept[Exception] {
      spark.read.option("header", "true").csv(invalidPath)
    }

    exception.getMessage should include("Path does not exist")
  }

  it should "correctly read a CSV file with missing values" in {
    val data = Seq(
      ("2021-01-01 00:00:00", "station1", 40.7128, -74.0060, "member"),
      ("2021-01-02 01:00:00", null, 34.0522, null, "casual")
    )

    val columns = Seq("ride_start_time", "station_name", "latitude", "longitude", "rider_type")
    val df = spark.createDataFrame(data).toDF(columns: _*)

    df.na.fill("Unknown").count() shouldEqual 2
//    df.na.fill("Unknown").filter($"station_name" === "Unknown").count() shouldEqual 1
  }

  it should "correctly apply transformations to DataFrame" in {
    val data = Seq(
      ("2021-01-01 00:00:00", "station1", 40.7128, -74.0060, "member"),
      ("2021-01-02 01:00:00", "station2", 34.0522, -118.2437, "casual")
    )

    val columns = Seq("ride_start_time", "station_name", "latitude", "longitude", "rider_type")
    val df = spark.createDataFrame(data).toDF(columns: _*)

//    val transformedDF = df.withColumn("location", $"latitude" + $"longitude")

//    transformedDF.columns should contain("location")
//    transformedDF.select("location").collect().map(_.getDouble(0)) shouldEqual Array(-33.2932, -83.1915)
  }

  it should "aggregate data by rider_type" in {
    val data = Seq(
      ("2021-01-01 00:00:00", "station1", 40.7128, -74.0060, "member"),
      ("2021-01-01 01:00:00", "station1", 40.7128, -74.0060, "member"),
      ("2021-01-02 01:00:00", "station2", 34.0522, -118.2437, "casual")
    )

    val columns = Seq("ride_start_time", "station_name", "latitude", "longitude", "rider_type")
    val df = spark.createDataFrame(data).toDF(columns: _*)

    val aggregatedDF = df.groupBy("rider_type").count()

    aggregatedDF.collect().map(row => (row.getString(0), row.getLong(1))) should contain theSameElementsAs Array(
      ("member", 2),
      ("casual", 1)
    )
  }

  it should "handle data with incorrect data types" in {
    val data = Seq(
      ("2021-01-01 00:00:00", "station1", "invalid_latitude", "-74.0060", "member")
    )

    val columns = Seq("ride_start_time", "station_name", "latitude", "longitude", "rider_type")
    val df = spark.createDataFrame(data).toDF(columns: _*)

    val exception = intercept[Exception] {
      df.withColumn("latitude", df("latitude").cast("double"))
    }

    exception.getMessage should include("for input string: \"invalid_latitude\"")
  }

  it should "correctly handle duplicate rows" in {
    val data = Seq(
      ("2021-01-01 00:00:00", "station1", 40.7128, -74.0060, "member"),
      ("2021-01-01 00:00:00", "station1", 40.7128, -74.0060, "member")
    )

    val columns = Seq("ride_start_time", "station_name", "latitude", "longitude", "rider_type")
    val df = spark.createDataFrame(data).toDF(columns: _*)

    val distinctDF = df.distinct()
    distinctDF.count() shouldEqual 1
  }

  override def afterAll(): Unit = {
    spark.stop()
  }
  // Add more specific tests based on your actual application logic here
}
