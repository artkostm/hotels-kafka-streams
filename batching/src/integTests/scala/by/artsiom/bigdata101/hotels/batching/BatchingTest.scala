package by.artsiom.bigdata101.hotels.batching

import java.io.File
import java.nio.file.Files
import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import by.artsiom.bigdata101.hotels.generator.converter.EventConverter
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpecLike
import by.artsiom.bigdata101.hotels.generator.publisher.RandomEventsPublisher
import org.apache.commons.io.FileUtils
import org.apache.kafka.common.serialization.ByteArraySerializer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.JavaConverters._

class BatchingTest extends TestKit(ActorSystem("batching_test")) with FlatSpecLike with EmbeddedKafka {
  import BatchingTest._

  implicit val mat = ActorMaterializer()

  implicit val kafkaConfig = EmbeddedKafkaConfig(kafkaPort = 12345, zooKeeperPort = 6543)
  implicit val kafkaSerializer = new ByteArraySerializer()

  def withConfig(kafkaConf: EmbeddedKafkaConfig)(test: Config => Unit): Unit = {
    val tmpDir = FileUtils.getTempDirectoryPath + File.separator + BatchingTest.TempDirectory + UUID.randomUUID().toString
    println("Temp file path: " + tmpDir)
    try test(Config(s"localhost:${kafkaConf.kafkaPort}", Topic, tmpDir))
    finally FileUtils.deleteQuietly(new File(tmpDir))
  }


  it should "successfully create parquet files from kafka messages" in withConfig(kafkaConfig) { config =>
    withRunningKafka {
      val messagesPublished = Source.fromPublisher(RandomEventsPublisher(10))
        .map(EventConverter(Topic))
        .runWith(Sink.foreach(msg => publishToKafka(msg)))

      assert(Await.result(messagesPublished, 10 seconds) == Done)

      Main.run(config)(SparkSession.builder.appName("batching-integ-test").master("local").getOrCreate())

      val files = Files.list(new File(config.outputDir).toPath).iterator().asScala
      assert(files.exists(_.getFileName.toString == "_SUCCESS"))
    }
  }
}

object BatchingTest {
  val Topic = "TopicTest"
  val TempDirectory = "tmp-batching"
  val Parallelism = 10
  val NumberOfEvents = 10
}
