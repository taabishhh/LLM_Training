import config.ConfigLoader
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkException}
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.slf4j.{Logger, LoggerFactory}
import java.io.FileNotFoundException

import scala.collection.mutable.ArrayBuffer

object SlidingWindowSpark {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def run(args: Array[String]): Unit = {
    // Set up Spark configuration and context
    logger.info("SlidingWindowSpark implementing...")
    try {
    val conf = new SparkConf().setAppName("Sliding Window Dataset").setMaster("local[*]")
    val spark = SparkSession.builder().config(conf).getOrCreate()
    val sc = new JavaSparkContext(spark.sparkContext)

    // Load your CSV dataset
    val csvFilePath = ConfigLoader.inputPath // Update with your actual input path

      val embeddingsDF: DataFrame = spark.read.option("header", "true").csv(csvFilePath)

    // Convert DataFrame to Array of Tokens and Embeddings
    val tokenEmbeddings: Array[(String, Array[Double])] = embeddingsDF.collect().map(row => {
      val token = row.getString(0)
      val embeddings = (1 until row.size).map(i => row.getString(i).toDouble).toArray
      (token, embeddings)
    })

    // Combine tokens into sentences, assuming the period is the delimiter
    val sentences: Array[String] = tokenEmbeddings.map(_._1).mkString(" ").split("\\.").map(_.trim).filter(_.nonEmpty)

    // Create a mapping of sentences to their words and embeddings
    val sentenceWordsAndEmbeddings: Array[(Array[String], Array[Array[Double]])] = sentences.map(sentence => {
      val wordsInSentence = sentence.split(" ") // Split the sentence into words
      val embeddingsInSentence = wordsInSentence.map(word => {
        tokenEmbeddings.find(_._1 == word).map(_._2).getOrElse(Array.empty[Double]) // Get the corresponding embeddings or empty if not found
      })
      (wordsInSentence, embeddingsInSentence)
    })

    // Log words and their embeddings
    sentenceWordsAndEmbeddings.foreach { case (words, embeddings) =>
      logger.info(s"Words: ${words.mkString(", ")}")
      logger.info(s"Embeddings: ${embeddings.map(_.mkString(",")).mkString("; ")}")
    }


    // Define window size
    val windowSize = 2

    // Parallelize the token embeddings into an RDD
    //    val tokenEmbeddingsRDD = sc.parallelize(tokenEmbeddings.toList)
    val tokenEmbeddingsRDD = sc.parallelize(sentenceWordsAndEmbeddings.toList)

    // Create sliding windows with embeddings in parallel
    val slidingWindowsRDD = tokenEmbeddingsRDD.flatMap { case (sentence, embeddings) =>
      createSlidingWindowsWithPositionalEmbedding(sentence, embeddings, windowSize)
    }
    //
    //    // Collect and log the results
    val collectedResults = slidingWindowsRDD.collect()
    collectedResults.foreach { case (inputWindowTokens, inputEmbeddings, targetToken, targetEmbedding) =>
      logger.info(s"Input Window Tokens: ${inputWindowTokens.mkString(", ")}")
      logger.info(s"Input Embeddings: $inputEmbeddings") // Assuming inputEmbeddings is an INDArray
      logger.info(s"Target Token: $targetToken")
      logger.info(s"Target Embedding: $targetEmbedding") // Assuming targetEmbedding is an INDArray
      logger.info("-----")
    }

    // Prepare data for CSV
    import spark.implicits._

    val csvData = collectedResults.map { case (inputWindowTokens, inputEmbeddings, targetToken, targetEmbedding) =>
      (
        inputWindowTokens.mkString(","), // Convert tokens to string
        inputEmbeddings.data().asDouble().mkString(","), // Convert INDArray to string
        targetToken,
        targetEmbedding.data().asDouble().mkString(",") // Convert INDArray to string
      )
    }

    // Convert to DataFrame
    val csvDF = csvData.toSeq.toDF("inputWindowTokens", "inputEmbeddings", "targetToken", "targetEmbedding")

    // Save the DataFrame to a single CSV file without header
    val tempDir = s"${ConfigLoader.outputPath}/temp"
    csvDF.coalesce(1)
      .write
      .option("header", "false") // Disable header
      .mode("overwrite")
      .csv(tempDir) // Specify the output path

    // Move and rename the file
    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val tempFile = fs.globStatus(new Path(s"$tempDir/part*"))(0).getPath.getName
    val destPath = new Path(s"${ConfigLoader.outputPath}/sliding_window_data.csv")

    // Rename to the desired file name
    fs.rename(new Path(s"$tempDir/$tempFile"), destPath)

    // Delete the temporary directory
    fs.delete(new Path(tempDir), true)

    logger.info("Data saved to sliding_window_data.csv successfully!")
    logger.info("Data saved to CSV successfully!")

    // Stop the Spark context
    sc.stop()
    } catch {
      case e: SparkException =>
        println(s"An error occurred while reading the CSV file: ${e.getMessage}. no such file")
      case p: FileNotFoundException =>
        println(s"An unexpected error occurred: ${p.getMessage}. no such file")
    }
  }

  //  def createSlidingWindowsWithPositionalEmbedding(tokens: Array[String], embeddings: Array[Double], windowSize: Int): Seq[(Array[String], INDArray, String, INDArray)] = {
  //    val dataSetList = ArrayBuffer[(Array[String], INDArray, String, INDArray)]()
  //
  //    // Check if the embeddings array is large enough
  //    if (embeddings.length >= windowSize) {
  //      // Create sliding windows
  //      for (i <- 0 until (tokens.length - windowSize)) {
  //        // Extract input window tokens
  //        val inputWindowTokens = tokens.slice(i, i + windowSize) // Get tokens for the current window
  //        val inputWindowEmbeddings = embeddings.slice(i * embeddings.length / tokens.length, (i + windowSize) * embeddings.length / tokens.length) // Ensure correct size
  //
  //        // Ensure correct length for reshaping
  //        if (inputWindowEmbeddings.length == windowSize * (embeddings.length / tokens.length)) {
  //          val embeddingDim = embeddings.length / tokens.length // Calculate embedding dimension
  //          val inputEmbeddings = Nd4j.create(inputWindowEmbeddings).reshape(windowSize, embeddingDim) // Reshape appropriately
  //
  //          // Get the target token and its embedding
  //          val targetToken = tokens(i + windowSize) // Get the target token
  //          val targetEmbeddingArray = embeddings((i + windowSize) * (embeddings.length / tokens.length)) // Get the target embedding
  //          val targetEmbedding = Nd4j.create(Array(targetEmbeddingArray)) // Create INDArray for target embedding
  //
  //          // Add positional embeddings to the word embeddings
  //          val positionalEmbeddings = computePositionalEmbedding(windowSize, embeddingDim)
  //          val positionAwareEmbedding = inputEmbeddings.add(positionalEmbeddings)
  //
  //          // Add to the dataset (input includes tokens and embeddings, target is the next word and its embedding)
  //          dataSetList.append((inputWindowTokens, positionAwareEmbedding, targetToken, targetEmbedding))
  //        } else {
  //          logger.warn(s"Skipping window creation for index $i due to mismatched embedding size.")
  //        }
  //      }
  //    }
  //
  //    dataSetList.toSeq
  //  }
  def createSlidingWindowsWithPositionalEmbedding(tokens: Array[String], embeddings: Array[Array[Double]], windowSize: Int): Seq[(Array[String], INDArray, String, INDArray)] = {
    val dataSetList = ArrayBuffer[(Array[String], INDArray, String, INDArray)]()

    // Check if there are enough tokens and embeddings
    if (tokens.length >= windowSize && embeddings.length >= tokens.length) {
      // Create sliding windows
      for (i <- 0 until (tokens.length - windowSize)) {
        // Extract input window tokens
        val inputWindowTokens = tokens.slice(i, i + windowSize) // Get tokens for the current window

        // Get corresponding embeddings for the current window
        val inputWindowEmbeddings = embeddings.slice(i, i + windowSize) // Get embeddings for the current window

        // Ensure correct length for reshaping
        if (inputWindowEmbeddings.length == windowSize) {
          val embeddingDim = embeddings(0).length // Calculate embedding dimension based on the first embedding's length
          val inputEmbeddings = Nd4j.create(inputWindowEmbeddings.map(_.toArray)).reshape(windowSize, embeddingDim) // Reshape appropriately

          // Get the target token and its embedding
          val targetToken = tokens(i + windowSize) // Get the target token
          val targetEmbedding = embeddings(i + windowSize) // Get the target embedding
          val targetEmbeddingNDArray = Nd4j.create(targetEmbedding) // Create INDArray for target embedding

          // Add positional embeddings to the word embeddings
          val positionalEmbeddings = computePositionalEmbedding(windowSize, embeddingDim)
          val positionAwareEmbedding = inputEmbeddings.add(positionalEmbeddings)

          // Add to the dataset (input includes tokens and embeddings, target is the next word and its embedding)
          dataSetList.append((inputWindowTokens, positionAwareEmbedding, targetToken, targetEmbeddingNDArray))
        } else {
          logger.warn(s"Skipping window creation for index $i due to mismatched embedding size.")
        }
      }
    }

    dataSetList.toSeq
  }

  def computePositionalEmbedding(windowSize: Int, embeddingDim: Int): INDArray = {
    val positionalEncoding = Nd4j.zeros(windowSize, embeddingDim)

    for (pos <- 0 until windowSize) {
      for (i <- 0 until embeddingDim by 2) {
        val angle = pos.toDouble / Math.pow(10000, (2.0 * i) / embeddingDim)
        positionalEncoding.putScalar(Array(pos, i), Math.sin(angle))
        if (i + 1 < embeddingDim) { // Ensure we don't go out of bounds
          positionalEncoding.putScalar(Array(pos, i + 1), Math.cos(angle))
        }
      }
    }

    positionalEncoding
  }

  case class SlidingWindowData(inputWindowTokens: Array[String], inputEmbeddings: Array[Double], targetToken: String, targetEmbedding: Array[Double])
}
