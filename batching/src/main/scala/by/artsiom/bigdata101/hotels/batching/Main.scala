package by.artsiom.bigdata101.hotels.batching
import java.io.ByteArrayInputStream

import by.artsiom.bigdata101.hotels.model.Event
import com.sksamuel.avro4s.{AvroInputStream, AvroSchema}
import org.apache.avro.Schema
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.BinaryType

object Main extends App {

  args match {
    case Array(brockerList, topicName, outputDir) =>
      val spark = SparkSession.builder
        .master("local[*]")
        .appName("hotels-streaming")
        .getOrCreate()

      val eventCountAcc = spark.sparkContext.longAccumulator("event_count")

      val kafkaDF = spark.read
        .format("kafka")
        .option("kafka.bootstrap.servers", brockerList)
        .option("subscribe", topicName)
        .option("startingOffsets", "earliest")
        .load()

      import spark.implicits._
      import Event._

      implicit val eventSchema = AvroSchema[Event]

      val rowEventSchema = eventSchema.toString

      kafkaDF
        .coalesce(2)
        .select($"value".cast(BinaryType).as("event"))
        .map(row => {
          val bais = new ByteArrayInputStream(row.getAs[Array[Byte]]("event"))
          val input = AvroInputStream.binary[Event].from(bais).build(new Schema.Parser().parse(rowEventSchema))
          eventCountAcc.add(1)
          input.iterator.next()
        })
        .write
        .format("parquet")
        .option("path", outputDir)
        .option("checkpointLocation", s"$outputDir/checkpoint")
        .save()
    case _ =>
      sys.error("Usage: spark-submit --class Main --master <master> batching.jar <brocker-list> <topic-name> <out-dir>")
  }
}
