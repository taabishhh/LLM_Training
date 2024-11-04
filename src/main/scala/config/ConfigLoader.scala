package config

import com.typesafe.config.{Config, ConfigFactory}

object ConfigLoader {
  // Load the configuration from application.conf
  private val config: Config = ConfigFactory.load()

  // Access specific configurations
  val appName: String = config.getString("app.name")
  val appVersion: String = config.getString("app.version")

  // Default input/output paths (can be overridden by terminal args)
  var inputPath: String = config.getString("app.inputPath")
  var outputPath: String = config.getString("app.outputPath")
  var sparkMaster: String = config.getString("app.sparkMaster")

  // Function to override paths with command-line arguments
  def overridePaths(input: String, output: String): Unit = {
    inputPath = input
    outputPath = output
  }
}
