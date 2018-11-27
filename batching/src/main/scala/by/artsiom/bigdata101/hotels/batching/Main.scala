package by.artsiom.bigdata101.hotels.batching

import by.artsiom.bigdata101.hotels.HotelImplicits
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.BinaryType

object Main extends HotelImplicits {

  def main(args: Array[String]): Unit =
    args match {
      case Array(brokerList, topicName, outputDir) =>
        implicit val spark = SparkSession.builder
          .master("local[*]")
          .appName("hotels-streaming")
          .getOrCreate()

        val kafkaDF = spark.read
          .format("kafka")
          .option("kafka.bootstrap.servers", brokerList)
          .option("subscribe", topicName)
          .option("startingOffsets", "earliest")
          .load()

        import spark.implicits._

        kafkaDF
          .coalesce(2)
          .select($"value".cast(BinaryType).as("event"))
          .fromAvro("event")
          .write
          .format("parquet")
          .option("path", outputDir)
          .option("checkpointLocation", s"$outputDir/checkpoint")
          .save()
      case _ =>
        sys.error("Usage: spark-submit --class Main --master <master> batching.jar <brocker-list> <topic-name> <out-dir>")
    }
}
