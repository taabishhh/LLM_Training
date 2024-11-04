
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.13"

lazy val root = (project in file("."))
  .settings(
    name := "Exercises441"
  )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) =>
    xs match {
      case "MANIFEST.MF" :: sNil => MergeStrategy.discard
      case  "services" :: _     => MergeStrategy.concat
      case _                    => MergeStrategy.discard
    }
  case "reference.conf" => MergeStrategy.concat
  case x if x.endsWith(".proto") => MergeStrategy.rename
  case x if x.contains("hadoop") => MergeStrategy.first
  case _ => MergeStrategy.first
}

// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-common
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.3.6"
// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-mapreduce-client-core
libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.6"
libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % "3.3.6"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.3"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.3"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.5.3"
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M2.1"
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-nlp" % "1.0.0-M2.1"
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-nn" % "0.9.1"
libraryDependencies += "org.deeplearning4j" %% "dl4j-spark" % "1.0.0-M2.1"
libraryDependencies += "org.deeplearning4j" %% "dl4j-spark-parameterserver" % "1.0.0-M2.1"
libraryDependencies += "org.nd4j" % "nd4j-native-platform" % "1.0.0-M2.1"
libraryDependencies += "org.nd4j" % "nd4j-api" % "1.0.0-M2.1"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.6"
libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.12"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"
libraryDependencies += "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % "test"
libraryDependencies += "com.typesafe" % "config" % "1.4.3"
libraryDependencies += "org.apache.commons" % "commons-csv" % "1.9.0"
libraryDependencies += "org.tensorflow" % "tensorflow" % "1.15.0"

// Specify the repository for Spark dependencies
resolvers ++= Seq(
  "Apache Repository" at "https://repo1.maven.org/maven2/",
  "Spark Packages Repo" at "https://repos.spark-packages.org/",
  "Confluent" at "https://packages.confluent.io/maven/",
  "Sonatype OSS" at "https://oss.sonatype.org/content/repositories/releases/",
  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  "jitpack" at "https://jitpack.io",
  "Deeplearning4j Repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)