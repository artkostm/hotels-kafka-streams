package by.artsiom.bigdata101.hotels.streaming
import java.io.File
import java.nio.file.Files
import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import by.artsiom.bigdata101.hotels.generator.converter.EventConverter
import by.artsiom.bigdata101.hotels.generator.publisher.RandomEventsPublisher
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.commons.io.FileUtils
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.JavaConverters._

class StreamingTest
    extends TestKit(ActorSystem("streamint_test"))
    with FlatSpecLike
    with EmbeddedKafka {
  import StreamingTest._

  implicit val mat             = ActorMaterializer()
  implicit val kafkaSerializer = new ByteArraySerializer()
  implicit val dispatcher      = system.dispatcher

  val kafkaConfig = EmbeddedKafkaConfig()

  def withConfig(kafkaConf: EmbeddedKafkaConfig)(test: Config => Unit): Unit = {
    val tmpDir = FileUtils.getTempDirectoryPath + File.separator + TempDirectoryPrefix + UUID
      .randomUUID()
      .toString
    println("Temp file path: " + tmpDir)
    try test(Config(s"localhost:${kafkaConf.kafkaPort}", Topic, tmpDir, "earliest"))
    finally FileUtils.deleteQuietly(new File(tmpDir))
  }

  it should "successfully stream parquet files from kafka" in withConfig(kafkaConfig) { jobConfig =>
    withRunningKafkaOnFoundPort(kafkaConfig) { implicit kafkaConfigWithPorts =>
      val messagesPublished = Source
        .fromPublisher(RandomEventsPublisher(NumberOfEvents))
        .map(EventConverter(Topic))
        .runWith(Sink.foreach(msg => publishToKafka(msg)))

      assert(Await.result(messagesPublished, 10 seconds) == Done)

      val spark = SparkSession.builder.appName("streaming-integ-test").master("local").getOrCreate()
      system.scheduler.scheduleOnce(10 seconds) {
        spark.streams.active.foreach(_.stop())
      }
      Main.run(jobConfig.copy(brokerList = s"localhost:${kafkaConfigWithPorts.kafkaPort}"))(
        spark
      )

      val files = Files.list(new File(jobConfig.outputDir).toPath).iterator().asScala
      assert(files.exists(_.getFileName.toString.startsWith("part-0000")))
    }
  }
}

object StreamingTest {
  val Topic               = "StreamingTopicTest"
  val TempDirectoryPrefix = "tmp-streaming-"
  val NumberOfEvents      = 10
}
