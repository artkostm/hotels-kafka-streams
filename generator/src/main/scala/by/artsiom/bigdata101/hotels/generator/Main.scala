package by.artsiom.bigdata101.hotels.generator

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.stream.scaladsl.{Sink, Source}

object Main extends App {
  implicit val system = ActorSystem("hotel_events_generator")
  implicit val actorMaterializer = ActorMaterializer()

//  val producerSettings = ProducerSettings(system, None, None).withParallelism(10)

  var count = 0
  Source
    .fromPublisher(RandomEventsPublisher(100000))
    .to(Sink.foreach(e => {count = count + 1;println(s"$count - $e")}))
    .run()
}
