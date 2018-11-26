package main.scala.by.artsiom.bigdata101.hotels.streaming

import by.artsiom.bigdata101.hotels.HotelImplicits
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.BinaryType

object Main extends App with HotelImplicits {

  args match {
    case Array(brokerList, topicName, outputDir) =>
      implicit val spark = SparkSession.builder
        .master("local[*]")
        .appName("hotels-streaming")
        .getOrCreate()

      val kafkaStreamDF = spark.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", brokerList)
        .option("subscribe", topicName)
        .option("startingOffsets", "latest")
        .load()

      import spark.implicits._

      kafkaStreamDF
        .coalesce(2)
        .select($"value".cast(BinaryType).as("event"))
        .fromAvro("event")
        .writeStream
        .format("es")
        .queryName("stream_to_elastic")
        .outputMode(OutputMode.Append)
        .option("es.nodes", "localhost")
        .option("es.nodes.wan.only", "true")
        .option("es.index.auto.create", "true")
        .option("checkpointLocation", s"$outputDir/checkpoint")
        .start(outputDir)
        .awaitTermination()
    case _ =>
      sys.error(
        "Usage: spark-submit --class Main --master <master> elastic.jar <brocker-list> <topic-name> <out-dir>"
      )
  }
}
