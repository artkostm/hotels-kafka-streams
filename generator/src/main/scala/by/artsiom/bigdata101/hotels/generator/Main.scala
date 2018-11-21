package by.artsiom.bigdata101.hotels.generator

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import by.artsiom.bigdata101.hotels.generator.converter.EventConverter
import by.artsiom.bigdata101.hotels.generator.publisher.RandomEventsPublisher
import by.artsiom.bigdata101.hotels.model.Event
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.util.{Failure, Success}

object Main extends App with Generator with ConfigurationAware {
  override protected implicit val system: ActorSystem = ActorSystem(
    "hotel_events_generator")

  implicit val actorMaterializer = ActorMaterializer()
  implicit val global = system.dispatcher

  val producerSettings = ProducerSettings[String, String](system,
                                                          new StringSerializer,
                                                          new StringSerializer)

  val producerRecordFlow =
    Flow.fromFunction[Event, Message](EventConverter(topic()))

  val doneFuture = generate(RandomEventsPublisher(numberOfEvents()),
                            producerRecordFlow,
                            Sink.foreach(println))

//    .runWith(Producer.plainSink(producerSettings))
//    .runWith(Sink.foreach(_ => ()))

  doneFuture.onComplete(done => {
    done match {
      case Success(value)     => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    system.terminate()
  })

  Await.ready(system.whenTerminated, Duration(1, TimeUnit.MINUTES))
}
