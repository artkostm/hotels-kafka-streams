package by.artsiom.bigdata101.hotels.generator

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{GraphDSL, RunnableGraph}
import by.artsiom.bigdata101.hotels.generator.config.Configuration
import by.artsiom.bigdata101.hotels.model.Event

import scala.concurrent.duration.FiniteDuration

trait Pipeline {
  protected val system: ActorSystem

  def create(
    eventSource: Graph[SourceShape[Event], _],
    eventConverter: Graph[FlowShape[Event, Message], _],
    sink: Graph[SinkShape[Message], Mat]
  )(implicit materializer: ActorMaterializer): RunnableGraph[Mat] =
    RunnableGraph.fromGraph(GraphDSL.create(sink) { implicit builder => s =>
      import GraphDSL.Implicits._

      eventSource ~> eventConverter ~> s.in
      ClosedShape
    })
}

trait ConfigurationAware { self: Pipeline =>
  def topic(): String                             = Configuration(system).TopicName
  def parallelism(): Int                          = Configuration(system).Parallelism
  def numberOfEvents(): Int                       = Configuration(system).NumberOfEvents
  def throttling(): Option[(Int, FiniteDuration)] = Configuration(system).Throttling.toOption
}
