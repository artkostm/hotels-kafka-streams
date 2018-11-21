package by.artsiom.bigdata101.hotels.generator

import java.util.concurrent.{TimeUnit, TimeoutException}

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.Flow
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

  val decider: Supervision.Decider = error => {
    system.log.error(error, "Exception handled: " + error.getMessage)
    error match {
      case _: TimeoutException => Supervision.Stop
      case _                   => Supervision.Stop
    }
  }

  implicit val actorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))
  implicit val global = system.dispatcher

  val producerSettings = ProducerSettings[String, String](system,
                                                          new StringSerializer,
                                                          new StringSerializer)

  val producerRecordFlow =
    Flow.fromFunction[Event, Message](EventConverter(topic()))

  val doneFuture = generate(RandomEventsPublisher(numberOfEvents()),
                            producerRecordFlow,
                            Producer.plainSink(producerSettings))

  doneFuture.onComplete(done => {
    done match {
      case Success(value)     => system.log.info(value.toString)
      case Failure(exception) => exception.printStackTrace()
    }
    system.terminate()
  })

  Await.ready(system.whenTerminated, Duration(1, TimeUnit.MINUTES))
}
