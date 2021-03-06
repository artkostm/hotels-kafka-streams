import sbt._

object Dependencies {

  val versions = new {
    val randomDataGenerator = "2.6"
    val alpakka             = "0.22"
    val akkaMonitor         = "0.1.1"
    val akka                = "2.5.13"

    val elasticsearch = "6.5.1"
    val spark         = "2.3.0"
    val avro4s        = "2.0.2"

    val scalaTest       = "3.0.5"
    val scalaCheck      = "1.14.0"
    val scalaMock       = "4.1.0"
    val embeddedKafka   = "2.0.0"
    val embeddedElastic = "2.7.0"
    val jackson         = "2.9.6"
  }

  lazy val spark = Seq(
    "org.apache.spark" %% "spark-core" % versions.spark % Provided,
    "org.apache.spark" %% "spark-sql"  % versions.spark % Provided
  )

  lazy val common = Seq(
    "com.sksamuel.avro4s" %% "avro4s-kafka" % versions.avro4s,
    "com.sksamuel.avro4s" %% "avro4s-core"  % versions.avro4s
  )

  lazy val sparkCommon = spark ++ Seq(
    "org.apache.spark" %% "spark-sql-kafka-0-10" % versions.spark % Provided
  )

  lazy val sparkStreaming = spark ++ Seq(
    "org.apache.spark" %% "spark-streaming" % versions.spark % Provided
  )

  lazy val generatorModule = Seq(
    "com.danielasfregola" %% "random-data-generator" % versions.randomDataGenerator,
    "com.typesafe.akka"   %% "akka-stream-kafka"     % versions.alpakka,
    "com.github.artkostm" %% "akka-stream-mon"       % versions.akkaMonitor
  )

  lazy val streamingModule = sparkStreaming ++ Seq(
    "org.apache.spark" %% "spark-streaming-kafka-0-10" % versions.spark,
    "org.apache.spark" %% "spark-sql-kafka-0-10"       % versions.spark
  )

  lazy val batchingModule = spark ++ Seq(
    "org.apache.spark" %% "spark-sql-kafka-0-10" % versions.spark
  )

  lazy val elasticModule = streamingModule ++ Seq(
    "org.elasticsearch" %% "elasticsearch-spark-20" % versions.elasticsearch
  )

  lazy val commonTest = Seq(
    "org.scalatest"  %% "scalatest"  % versions.scalaTest,
    "org.scalacheck" %% "scalacheck" % versions.scalaCheck,
    "org.scalamock"  %% "scalamock"  % versions.scalaMock
  )

  lazy val generatorTests = commonTest ++ Seq(
    "com.typesafe.akka"   %% "akka-stream-testkit" % versions.akka,
    "org.reactivestreams" % "reactive-streams-tck" % "1.0.2"
  )

  lazy val integTests = commonTest ++ Seq(
    "net.manub"         %% "scalatest-embedded-kafka" % versions.embeddedKafka,
    "pl.allegro.tech"   % "embedded-elasticsearch"    % versions.embeddedElastic,
    "com.typesafe.akka" %% "akka-stream-testkit"      % versions.akka
  )

  lazy val overrides = Seq(
    "com.fasterxml.jackson.core"   % "jackson-core"          % versions.jackson,
    "com.fasterxml.jackson.core"   % "jackson-databind"      % versions.jackson,
    "com.fasterxml.jackson.core"   % "jackson-annotations"   % versions.jackson,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % versions.jackson
  )
}
