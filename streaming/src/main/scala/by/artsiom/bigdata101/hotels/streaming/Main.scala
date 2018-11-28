package by.artsiom.bigdata101.hotels.streaming

import by.artsiom.bigdata101.hotels.{HotelImplicits, HotelsApp, InitError}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.BinaryType

object Main extends HotelsApp[Config] with HotelImplicits {

  override def run(config: Config)(implicit spark: SparkSession): Unit = {
    val kafkaStreamDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", config.brokerList)
      .option("subscribe", config.topicName)
      .option("startingOffsets", "latest")
      .load()

    import spark.implicits._

    kafkaStreamDF
      .coalesce(2)
      .select($"value".cast(BinaryType).as("event"))
      .fromAvro("event")
      .writeStream
      .format("parquet")
      .queryName("parquet_files_to_hdfs")
      .outputMode(OutputMode.Append())
      .option("path", config.outputDir)
      .option("checkpointLocation", "/tmp/hotels_streaming/")
      .start()
      .awaitTermination()
  }

  override def setup(args: Array[String]): Either[InitError, (SparkSession, Config)] =
    args match {
      case Array(brokerList, topicName, outputDir) =>
        Right(
          (
            SparkSession.builder
              .master("local[*]")
              .appName("hotels-streaming")
              .getOrCreate(),
            Config(brokerList, topicName, outputDir)
          )
        )
      case _ =>
        Left(
          InitError(
            "Usage: spark-submit --class Main --master <master> streaming.jar <brocker-list> <topic-name> <out-dir>"
          )
        )
    }
}

final case class Config(brokerList: String, topicName: String, outputDir: String)
