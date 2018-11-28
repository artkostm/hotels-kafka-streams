package by.artsiom.bigdata101.hotels.batching

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
import org.apache.kafka.common.serialization.ByteArraySerializer

import scala.concurrent.Await
import scala.concurrent.duration._

class BatchingTest extends TestKit(ActorSystem("batching_test")) with FlatSpecLike with EmbeddedKafka {
  import BatchingTest._

  implicit val mat = ActorMaterializer()

  implicit val kafkaConfig = EmbeddedKafkaConfig(kafkaPort = 12345, zooKeeperPort = 6543)
  implicit val kafkaSerializer = new ByteArraySerializer()

  def withConfig(kafkaConf: EmbeddedKafkaConfig)(test: Config => Unit): Unit =
    test(Config(s"localhost:${kafkaConf.kafkaPort}", Topic, TempDirectory))

  it should "be good" in withConfig(kafkaConfig) { config =>
    withRunningKafka {
      val messagesPublished = Source.fromPublisher(RandomEventsPublisher(10))
        .map(EventConverter(Topic))
        .runWith(Sink.foreach(msg => publishToKafka(msg)))

      assert(Await.result(messagesPublished, 5 seconds) == Done)

      Main.run(config)(SparkSession.builder.appName("batching-integ-test").master("local").getOrCreate())

      Thread.sleep(100000)
    }
  }
}

object BatchingTest {
  val Topic = "TopicTest"
  val TempDirectory = "tmp"
  val Parallelism = 10
  val NumberOfEvents = 10
}