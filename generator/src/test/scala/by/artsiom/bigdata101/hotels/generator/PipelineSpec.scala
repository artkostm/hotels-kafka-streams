package by.artsiom.bigdata101.hotels.generator

import java.sql.{Date, Timestamp}
import java.util.{Date => UDate}

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import by.artsiom.bigdata101.hotels.generator.converter.EventConverter
import by.artsiom.bigdata101.hotels.model.Event
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class PipelineSpec extends TestKit(ActorSystem("generator_test", ConfigFactory.parseString(
  s"""
     |akka {
     |  kafka.producer {
     |    parallelism = ${PipelineSpec.Parallelism}
     |    topic.name = ${PipelineSpec.Topic}
     |  }
     |}
     |
     |generator {
     |  number-of-events = ${PipelineSpec.NumberOfEvents}
     |  throttling {
     |    elements = ${PipelineSpec.Events}
     |    per = 10s
     |  }
     |}
     |
     """.stripMargin))) with ImplicitSender with FlatSpecLike with BeforeAndAfterAll with Matchers with Pipeline with ConfigurationAware {
  implicit val mat = ActorMaterializer()
  import PipelineSpec._


  "Pipeline" should "have correct configuration" in {
    assert(numberOfEvents() == NumberOfEvents)
    assert(parallelism() == Parallelism)
    assert(topic() == Topic)
    assert(throttling() == Some(Events, 10 seconds))
  }

  "Pipeline" should "move events from source to sink" in {
    val done = create(
      Source.fromIterator(() => List(testEvent()).toIterator),
      Flow.fromFunction(_ => new Message(Topic, Array.empty[Byte])),
      Sink.foreach { message =>
        assert(message.topic() == Topic)
      }
    ).run()

    assert(Await.result(done, 2 seconds) == Done)
  }

  "Pipeline" should "convert events to kafka messages" in {
    val done = create(
      Source.fromIterator(() => List(testEvent()).toIterator),
      Flow.fromFunction(EventConverter(Topic)),
      Sink.foreach { message =>
        assert(message.topic() == Topic)
        assert(!message.value().isEmpty)
      }
    ).run()

    assert(Await.result(done, 2 seconds) == Done)
  }

  def testEvent() = Event(
    new Timestamp(new UDate().getTime),
    1, 2, 3, 4, 5, 6.0F, 7, true, false,
    8, new Date(new UDate().getTime),
    new Date(new UDate().getTime), 9, 10,
    11, 12, 13, false, 14, 15, 16, 17, 18
  )
}

object PipelineSpec {
  val Topic = "test"
  val Parallelism = 10
  val NumberOfEvents = 5000
  val Events = 1
}
