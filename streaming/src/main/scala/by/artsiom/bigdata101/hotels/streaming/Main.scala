package by.artsiom.bigdata101.hotels.streaming

import by.artsiom.bigdata101.hotels.HotelImplicits
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.BinaryType

object Main extends App with HotelImplicits {

  args match {
    case Array(brockerList, topicName, outputDir) =>
      implicit val spark = SparkSession.builder
        .master("local[*]")
        .appName("hotels-streaming")
        .getOrCreate()

      val kafkaStreamDF = spark.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", brockerList)
        .option("subscribe", topicName)
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
        .option("path", outputDir)
        .option("checkpointLocation", s"$outputDir/checkpoint")
        .start()
        .awaitTermination()
    case _ =>
      sys.error(
        "Usage: spark-submit --class Main --master <master> streaming.jar <brocker-list> <topic-name> <out-dir>"
      )
  }
}
