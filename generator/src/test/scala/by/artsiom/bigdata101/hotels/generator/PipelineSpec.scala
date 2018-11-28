package by.artsiom.bigdata101.hotels.generator

import java.sql.{Date, Timestamp}
import java.util.{Date => UDate}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.{ImplicitSender, TestKit}
import by.artsiom.bigdata101.hotels.model.Event
import org.scalatest.{BeforeAndAfterAll, FlatSpec, FlatSpecLike, WordSpecLike}

class GeneratorSpec extends TestKit(ActorSystem("generator_test")) with ImplicitSender with FlatSpecLike with BeforeAndAfterAll {
  val Topic = "test"
  implicit val mat = ActorMaterializer()

  val generator = new Generator {
    override protected val system: ActorSystem = system
  }

  def test() = {

    generator.generate(TestSource.probe[Event], Flow.fromFunction(_ => new Message(Topic, Array.empty[Byte])), Sink.ignore)
  }

  def testEvent() = Event(
    new Timestamp(new UDate().getTime),
    1, 2, 3, 4, 5, 6.0F, 7, true, false,
    8, new Date(new UDate().getTime),
    new Date(new UDate().getTime), 9, 10,
    11, 12, 13, false, 14, 15, 16, 17, 18
  )
}
