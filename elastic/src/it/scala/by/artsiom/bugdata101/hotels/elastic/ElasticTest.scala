package by.artsiom.bugdata101.hotels.elastic

import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import by.artsiom.bigdata101.hotels.elastic.{Config, Main}
import by.artsiom.bigdata101.hotels.generator.converter.EventConverter
import by.artsiom.bigdata101.hotels.generator.publisher.RandomEventsPublisher
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import pl.allegro.tech.embeddedelasticsearch.{EmbeddedElastic, IndexSettings, PopularProperties}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.{Source => S}

class EcasticTest
  extends TestKit(ActorSystem("elastic_test"))
  with FlatSpecLike with EmbeddedKafka with BeforeAndAfterAll {
  import ElasticTest._

  implicit val mat             = ActorMaterializer()
  implicit val kafkaSerializer = new ByteArraySerializer()
  implicit val dispatcher      = system.dispatcher

  val kafkaConfig = EmbeddedKafkaConfig()

  val es = EmbeddedElastic.builder
    .withSetting(PopularProperties.TRANSPORT_TCP_PORT, 9300)
    .withSetting(PopularProperties.CLUSTER_NAME, "elastic-test")
    .withDownloadDirectory(new File("./es.tmp/"))
    .withInstallationDirectory(new File("./es.tmp/").getAbsoluteFile)
    .withCleanInstallationDirectoryOnStop(true)
    .withElasticVersion("6.5.1")
    .withStartTimeout(3, TimeUnit.MINUTES)
    .build()

  override protected def beforeAll(): Unit = es.start()

  def withConfig(kafkaConf: EmbeddedKafkaConfig)(test: Config => Unit): Unit = {
    try test(Config(s"localhost:${kafkaConf.kafkaPort}", Topic, IndexAndType, "localhost"))
    finally es.stop()
  }

  it should "successfully stream parquet files from kafka" in withConfig(kafkaConfig) {
    jobConfig =>
      withRunningKafkaOnFoundPort(kafkaConfig) { implicit kafkaConfigWithPorts =>
        val messagesPublished = Source
          .fromPublisher(RandomEventsPublisher(NumberOfEvents))
          .map(EventConverter(Topic))
          .runWith(Sink.foreach(msg => publishToKafka(msg)))

        assert(Await.result(messagesPublished, 10 seconds) == Done)

        val spark = SparkSession.builder.appName("streaming-integ-test").master("local").getOrCreate()
        system.scheduler.scheduleOnce(50 seconds) {
          spark.streams.active.foreach(_.stop())
        }
        Main.run(jobConfig)(
          spark
        )

        assert(es.fetchAllDocuments(Index).size() == NumberOfEvents)
      }
  }

  override protected def afterAll(): Unit = es.stop()
}

object ElasticTest {
  val Topic               = "TopicTest" + UUID.randomUUID().toString
  val Index               = "events"
  val Type                = "event"
  val IndexAndType        = s"$Index/$Type"
  val TempDirectoryPrefix = "tmp-elastic-"
  val Parallelism         = 10
  val NumberOfEvents      = 10
}