import org.slf4j.{Logger, LoggerFactory}

object MainApp {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    logger.info("Starting MainApp...")

    // Step 1: Run the SlidingWindowSpark to create and save sliding window data
    logger.info("Running SlidingWindowSpark...")
    SlidingWindowSpark.run(args)
    logger.info("SlidingWindowSpark completed successfully!")

    // Step 2: Train model using the generated sliding window data
    logger.info("Running TrainingWithSlidingWindow for model training...")
    TrainingWithSlidingWindowSpark.run(args)
    logger.info("TrainingWithSlidingWindow completed successfully!")

    logger.info("MainApp execution finished.")
  }
}
