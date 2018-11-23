package by.artsiom.bigdata101.hotels.generator

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{GraphDSL, RunnableGraph}
import by.artsiom.bigdata101.hotels.generator.config.Configuration
import by.artsiom.bigdata101.hotels.model.Event

trait Generator {
  protected val system: ActorSystem

  def generate(eventSource: Graph[SourceShape[Event], NotUsed],
               eventConverter: Graph[FlowShape[Event, Message], NotUsed],
               sink: Graph[SinkShape[Message], Mat])(
      implicit materializer: ActorMaterializer): RunnableGraph[Mat] =
    RunnableGraph.fromGraph(GraphDSL.create(sink) { implicit builder => s =>
      import GraphDSL.Implicits._
      eventSource ~> eventConverter ~> s.in
      ClosedShape
    })
}

trait ConfigurationAware { self: Generator =>
  def topic(): String = Configuration(system).TopicName
  def parallelism(): Int = Configuration(system).Parallelism
  def numberOfEvents(): Int = Configuration(system).NumberOfEvents
}
