import sbt._

object Dependencies {
  val versions = new {
    val randomDataGenerator = "2.6"
    val alpakka             = "0.22"
    val akkaMonitor         = "0.1.0"

    val spark  = "2.4.0"
    val avro4s = "2.0.2"
  }

  lazy val common = Seq(
    "com.sksamuel.avro4s" %% "avro4s-kafka" % versions.avro4s,
    "com.sksamuel.avro4s" %% "avro4s-core"  % versions.avro4s
  )

  lazy val sparkCommon = Seq(
    "org.apache.spark" %% "spark-sql-kafka-0-10" % versions.spark % Provided,
    "org.apache.spark" %% "spark-core"           % versions.spark % Provided,
    "org.apache.spark" %% "spark-sql"            % versions.spark % Provided
  )

  lazy val generatorModule = Seq(
    "com.danielasfregola" %% "random-data-generator" % versions.randomDataGenerator,
    "com.typesafe.akka"   %% "akka-stream-kafka"     % versions.alpakka,
    "net.ruippeixotog"    %% "akka-stream-mon"       % versions.akkaMonitor
  )

  lazy val streamingModule = Seq(
    "org.apache.spark" %% "spark-streaming-kafka-0-10" % versions.spark,
    "org.apache.spark" %% "spark-sql-kafka-0-10"       % versions.spark,
    "org.apache.spark" %% "spark-core"                 % versions.spark, // % Provided,
    "org.apache.spark" %% "spark-streaming"            % versions.spark, // % Provided,
    "org.apache.spark" %% "spark-sql"                  % versions.spark // % Provided
  )

  lazy val batchingModule = Seq(
    "org.apache.spark" %% "spark-sql-kafka-0-10" % versions.spark,
    "org.apache.spark" %% "spark-core"           % versions.spark, // % Provided,
    "org.apache.spark" %% "spark-sql"            % versions.spark // % Provided
  )
}
