package by.artsiom.bigdata101.hotels.elastic

import by.artsiom.bigdata101.hotels.{HotelImplicits, HotelsApp, InitError}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.BinaryType

object Main extends HotelsApp[Config] with HotelImplicits {
  override def run(implicit spark: SparkSession, config: Config): Unit = {
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
      .format("es")
      .queryName("stream_to_elastic")
      .outputMode(OutputMode.Append)
      .option("es.nodes", config.elasticNodes)
      .option("es.nodes.wan.only", "true")
      .option("es.mapping.date.rich", "false")
      .option("es.index.auto.create", "true")
      .option("checkpointLocation", "/tmp/hotels_elastic/")
      .start(config.indexAndType)
      .awaitTermination()
  }

  override def setup(args: Array[String]): Either[InitError, (SparkSession, Config)] =
    args match {
      case Array(brokerList, topicName, indexAndType, elasticNodes) =>
        Right(
          (
            SparkSession.builder
              .master("local[*]")
              .appName("hotels-streaming")
              .getOrCreate(),
            Config(brokerList, topicName, indexAndType, elasticNodes)
          )
        )
      case _ =>
        Left(
          InitError(
            "Usage: spark-submit --class Main --master <master> elastic.jar <brocker-list> <topic-name> <[index]/[type]> <elastic-nodes>"
          )
        )
    }
}

final case class Config(brokerList: String, topicName: String, indexAndType: String, elasticNodes: String)
