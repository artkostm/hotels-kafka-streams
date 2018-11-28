package by.artsiom.bigdata101.hotels.elastic

import by.artsiom.bigdata101.hotels.HotelImplicits
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.BinaryType

object Main extends HotelImplicits {

  def main(args: Array[String]): Unit =
    args match {
      case Array(brokerList, topicName, indexAndType, elasticNodes) =>
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
          .option("es.nodes", elasticNodes)
          .option("es.nodes.wan.only", "true")
          .option("es.mapping.date.rich", "false")
          .option("es.index.auto.create", "true")
          .option("checkpointLocation", "/tmp/hotels_elastic/")
          .start(indexAndType)
          .awaitTermination()
      case _ =>
        sys.error(
          "Usage: spark-submit --class Main --master <master> elastic.jar <brocker-list> <topic-name> <[index]/[type]> <elastic-nodes>"
        )
    }
}
