package by.artsiom.bigdata101.hotels.batching

import by.artsiom.bigdata101.hotels.{HotelImplicits, HotelsApp, InitError}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.BinaryType

final case class Config(brokerList: String,
                        topicName: String,
                        outputDir: String,
                        offset: String = "earliest")

object Main extends HotelsApp[Config] with HotelImplicits {
  override def run(config: Config)(implicit spark: SparkSession): Unit = {
    val kafkaDF = spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", config.brokerList)
      .option("subscribe", config.topicName)
      .option("startingOffsets", config.offset)
      .load()

    import spark.implicits._

    kafkaDF
      .coalesce(2)
      .select($"value".cast(BinaryType).as("event"))
      .fromAvro("event")
      .write
      .format("parquet")
      .option("path", config.outputDir)
      .option("checkpointLocation", "/tmp/hotel_batching/")
      .save()
    spark.stop()
  }

  override def setup(args: Array[String]): Either[InitError, (SparkSession, Config)] =
    args match {
      case Array(brokerList, topicName, outputDir) =>
        Right(
          (
            SparkSession.builder
              .master("local[*]")
              .appName("hotels-batching")
              .getOrCreate(),
            Config(brokerList, topicName, outputDir)
          )
        )
      case _ =>
        Left(
          InitError(
            "Usage: spark-submit --class Main --master <master> batching.jar <brocker-list> <topic-name> <out-dir>"
          )
        )
    }
}
