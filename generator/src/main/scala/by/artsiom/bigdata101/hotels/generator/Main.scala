package by.artsiom.bigdata101.hotels.generator

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.{Flow, Source}
import by.artsiom.bigdata101.hotels.generator.converter.EventConverter
import by.artsiom.bigdata101.hotels.generator.publisher.RandomEventsPublisher
import by.artsiom.bigdata101.hotels.model.Event
import net.ruippeixotog.streammon.ThroughputMonitor
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Failure, Success}

object Main extends App with Pipeline with ConfigurationAware {

  implicit override protected val system: ActorSystem = ActorSystem("hotel_events_generator")

  val decider: Supervision.Decider = error => {
    system.log.error(error, "Exception handled: " + error.getMessage)
    error match {
      case _ => Supervision.Stop
    }
  }

  implicit val actorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  )
  implicit val global = system.dispatcher

  val producerSettings =
    ProducerSettings[String, Array[Byte]](
      system,
      new StringSerializer,
      new ByteArraySerializer
    )

  val producerRecordFlow =
    Flow
      .fromFunction[Event, Message](EventConverter(topic()))
      .via(
        ThroughputMonitor(
          1 seconds,
          stat =>
            system.log.info(
              s"Processed events=${stat.count} Throughput=${"%.2f".format(stat.throughput)} ev/s"
          )
        )
      )

  val doneFuture = create(
    Source.fromPublisher(RandomEventsPublisher(numberOfEvents())),
    throttling().fold(producerRecordFlow)(t => producerRecordFlow.throttle(t._1, t._2)),
    Producer.plainSink(producerSettings)
  ).run()

  doneFuture.onComplete(done => {
    done match {
      case Success(value)     => system.log.info(value.toString)
      case Failure(exception) => exception.printStackTrace()
    }
    system.terminate()
  })

  Await.ready(system.whenTerminated, Duration(7, TimeUnit.MINUTES))
}
