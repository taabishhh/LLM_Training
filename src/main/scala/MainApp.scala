import config.ConfigLoader
import org.slf4j.{Logger, LoggerFactory}

object MainApp {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    logger.info("Starting MainApp...")
    val inputPath = args(0)
    val outputPath = args(1)

    ConfigLoader.overridePaths(inputPath, outputPath)

    // Print the final configuration
    ConfigLoader.printConfig()
    // Step 1: Run the SlidingWindowSpark to create and save sliding window data
    logger.info("Running SlidingWindowSpark...")
    SlidingWindowSpark.run(args)
    logger.info("SlidingWindowSpark completed successfully!")
    // Override the paths in the config with the command-line arguments


    // Step 2: Train model using the generated sliding window data and save the metrics
    logger.info("Running TrainingWithSlidingWindow for model training...")
    TrainingWithSlidingWindowSpark.run(args)
    logger.info("TrainingWithSlidingWindow completed successfully!")

    logger.info("MainApp execution finished.")
  }
}
