import config.ConfigLoader
import org.apache.commons.csv.{CSVFormat, CSVParser, CSVPrinter}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.slf4j.{Logger, LoggerFactory}

import java.io.{File, FileReader, FileWriter}
import scala.jdk.CollectionConverters._

object TrainingWithSlidingWindowSpark {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def run(args: Array[String]): Unit = {
    // Set up Spark configuration
    val sparkConf = new SparkConf()
      .setAppName("DL4J-Distributed-Training")
      .setMaster(ConfigLoader.sparkMaster)

    // Initialize Spark context
    val sc = new SparkContext(sparkConf)

    // Load sliding windows from CSV file
    val slidingWindows: List[DataSet] = loadSlidingWindowsFromCSV(s"${ConfigLoader.outputPath}/sliding_window_data.csv")

    // Parallelize the sliding windows to create an RDD
    val rddData: RDD[DataSet] = sc.parallelize(slidingWindows)

    // Load or create a model
    val model: MultiLayerNetwork = TransformerModel.createModel(1536, 64, 768) // Input size, hidden size, output size

    // Set up the TrainingMaster for distributed training
    val trainingMaster: ParameterAveragingTrainingMaster = new ParameterAveragingTrainingMaster.Builder(2)
      .batchSizePerWorker(2) // Batch size per worker
      .averagingFrequency(5) // Frequency of parameter averaging
      .workerPrefetchNumBatches(2)
      .build();

    // Initialize SparkDl4jMultiLayer with the model and training master
    val sparkModel = new SparkDl4jMultiLayer(sc, model, trainingMaster)

    // Set listeners to monitor the training progress
    model.setListeners(new ScoreIterationListener(10))

    // Train the model on the distributed RDD dataset
    sparkModel.fit(rddData)

    // Set up performance metrics
    val startTime = System.currentTimeMillis()
    val trainingStats = sparkModel.getScore.toString
    logger.info(s"trainingStats: $trainingStats")
    val endTime = System.currentTimeMillis()
    val learningRate = sparkModel.getNetwork.getLearningRate(0).toString
    val trainingDuration = endTime - startTime

    // Specify the file where the model will be saved
    val locationToSave = new File(s"${ConfigLoader.outputPath}/trained_model_spark.zip")
    val saveUpdater = true
    ModelSerializer.writeModel(sparkModel.getNetwork, locationToSave, saveUpdater)

    logger.info("Distributed training complete.")

    // Save training statistics and runtime measurements to CSV
    saveStatisticsToCSV(learningRate, trainingDuration, trainingStats, s"${ConfigLoader.outputPath}/training_statistics.csv")

    // Stop Spark context
    sc.stop()
  }

  private def saveStatisticsToCSV(learningRate: String, duration: Long, trainingStats: String, filePath: String): Unit = {
    val writer = new FileWriter(filePath)
    val csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader("Learning Rate, Training Duration (ms)", "Training Statistics").build())

    csvPrinter.printRecord(learningRate, duration.toString, trainingStats)
    csvPrinter.flush()
    csvPrinter.close()
  }

  def loadSlidingWindowsFromCSV(filePath: String): List[DataSet] = {
    val reader = new FileReader(filePath)
    val csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setDelimiter(',').setIgnoreSurroundingSpaces(false) // Helps ignore spaces around delimiters
      .setAllowMissingColumnNames(false) // Handles cases with missing columns
      .setQuote('"').build())// Ensure quotes are correctly managed.build())

    val dataSets = csvParser.getRecords.asScala.toList.map { record =>
      // Parse tokens and embeddings from columns
      val embedding1 = parseFeatureArray(record.get(1)) // Embedding for first token
      val embedding2 = parseFeatureArray(record.get(3)) // Embedding for second token

      // Concatenate embeddings as features for input
      val features = Nd4j.create(embedding1).reshape(1, embedding1.length) // Adjust as needed
      val labels = Nd4j.create(embedding2).reshape(1, embedding2.length) // Adjust as needed

      new DataSet(features, labels)
    }

    csvParser.close()
    reader.close()

    dataSets
  }

  private def parseFeatureArray(embeddingStr: String): Array[Double] = {
    embeddingStr.split(",").map(_.trim.toDouble)
  }
}
