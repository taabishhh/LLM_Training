# Homework2: NLP Training Pipeline with Apache Spark and DL4J

**Author:** Taabish Sutriwala  
**UIN:** 673379837  
**Email:** tsutr@uic.edu  

## Project Overview
This project implements a distributed pipeline for NLP model training using Apache Spark and DeepLearning4J (DL4J). The methodology utilizes a sliding window approach for data preparation, positional embeddings for token encoding, and Word2Vec model training with parallel processing. The model and training process is designed for scalability and optimized for large datasets.

### Methodology
1. **Data Preprocessing**:  
   - The dataset is loaded as CSV, with each row containing a token and its embeddings.  
   - Text is split into sentences, which are further segmented into words. Tokens and embeddings are grouped to form structured sentences.

2. **Sliding Window with Positional Embeddings**:  
   - A sliding window approach is applied to sentences, creating fixed-size windows for each sequence.  
   - Positional embeddings are added to account for token positions within the window, enhancing sequence understanding.

3. **Model Training (Word2Vec)**:  
   - The model trains using the sliding window embeddings as inputs and associated next tokens as target outputs.
   - Apache Spark enables distributed training, leveraging DL4J for efficient neural network operations.

4. **Performance Monitoring and Metrics**:  
   - During training, statistics like accuracy, loss, and runtime are logged for analysis.  
   - Additional metrics like convergence rate and model size provide insights into training effectiveness and efficiency.

### Partitioning
Data is partitioned by sentences, with each partition consisting of a series of tokens and corresponding embeddings. Each sliding window operation extracts a subset of tokens, embedding data for training input, and predicts the next token in the sequence.

### Input and Output
- **Input**: A CSV file containing token embeddings in the following format:
token,embedding_dim_0,embedding_dim_1,...,embedding_dim_n the,0.009552239,0.08198426,...,-0.32604042

- **Output**: A CSV file (`sliding_window_data.csv`) containing structured sliding window data, formatted as:
inputWindowTokens,inputEmbeddings,targetToken,targetEmbedding


### Installation

1. **Clone the Repository**:
 ```bash
 git clone <repository-url>
 cd Exercises441
```
2. Install Dependencies: Ensure SBT is installed. SBT will handle dependency resolution upon build.

3. Configure Paths: Update the input and output file paths in ConfigLoader.

4. Build the Project:
```
sbt clean compile
sbt assembly`
```
Running the Project
To execute the program:
```
sbt run
```
The application executes the following steps:

SlidingWindowSpark: Generates sliding window data with positional embeddings.
TrainingWithSlidingWindowSpark: Utilizes sliding window data for model training.

##Dependencies
```
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.13"

lazy val root = (project in file("."))
  .settings(
    name := "Exercises441"
  )

// Hadoop, Spark, DL4J, TensorFlow, CSV Handling, Logging
libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-common" % "3.3.6",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.6",
  "org.apache.spark" %% "spark-core" % "3.5.3",
  "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M2.1",
  "org.deeplearning4j" %% "dl4j-spark" % "1.0.0-M2.1",
  "org.tensorflow" % "tensorflow" % "1.15.0",
  "org.apache.commons" % "commons-csv" % "1.9.0",
  "ch.qos.logback" % "logback-classic" % "1.5.6",
  "org.scalatest" %% "scalatest" % "3.2.19" % "test"
)
```
###Performance Metrics Collection
**During training, the following statistics are logged for analysis:

Training Accuracy and Loss: Measures model convergence over epochs.
Runtime Performance: Captures model execution time for different stages.
Model Size and Parameters: Tracks model size and parameter count for resource evaluation.
Memory and CPU Utilization: Monitors system resource usage to assess load balancing and scalability.

###Repository Structure
Exercises441/
├── src/
│   ├── main/
│   │   └── scala/
│   │       ├── MainApp.scala
│   │       ├── SlidingWindowSpark.scala
│   │       └── TrainingWithSlidingWindowSpark.scala
├── resources/
│   └── application.conf
├── README.md
└── build.sbt

Notes
Ensure Apache Hadoop, Spark, and DL4J libraries are configured and accessible. Update ConfigLoader for dataset paths, and monitor log outputs for metric collection.

For additional information, contact: Taabish Sutriwala at tsutr@uic.edu.
