package by.artsiom.bigdata101.hotels.generator

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, FlowShape, Graph, SinkShape}
import akka.stream.scaladsl.{Flow, Source}
import by.artsiom.bigdata101.hotels.generator.config.Configuration
import by.artsiom.bigdata101.hotels.model.Event
import org.reactivestreams.Publisher

trait Generator {
  protected val system: ActorSystem

  def generate(eventsPublisher: Publisher[Event],
               eventConverter: Graph[FlowShape[Event, Message], NotUsed],
               sink: Graph[SinkShape[Message], Mat])(
      implicit materializer: ActorMaterializer): Mat =
    Source
      .fromPublisher(eventsPublisher)
      .via(eventConverter)
      .runWith(sink)
}

trait ConfigurationAware { self: Generator =>
  def topic(): String = Configuration(system).TopicName

  def parallelism(): Int = Configuration(system).Parallelism

  def numberOfEvents(): Int = Configuration(system).NumberOfEvents
}
