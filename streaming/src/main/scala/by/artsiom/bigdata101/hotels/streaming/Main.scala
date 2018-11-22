package by.artsiom.bigdata101.hotels.streaming

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{OutputMode, Trigger}


object Main extends App {


  val spark = SparkSession.builder.master("local[*]")
    .appName("hotels-test").getOrCreate()

  import spark.implicits._

  val records = spark.readStream
    .format("kafka")
    .option("subscribe", "TestTopic")
    .option("kafka.bootstrap.servers", "192.168.99.100:9092")
    .load().selectExpr("CAST(value AS STRING)").as[String]

  val sq = records.flatMap(_.split(" ")).groupBy("value").count().
    writeStream
    .outputMode("complete")
    .format("console")
    .start()

  sq.awaitTermination()
  sq.stop()

  spark.stop()
}
