package by.artsiom.bigdata101.hotels.generator.config

import java.util.concurrent.TimeUnit

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

private[config] class ConfigurationImpl(config: Config) extends Extension {

  val NumberOfEvents = config.getInt("generator.number-of-events")
  val Parallelism    = config.getInt("akka.kafka.producer.parallelism")
  val TopicName      = config.getString("akka.kafka.producer.topic.name")

  val Throttling = for {
    elements <- Try(config.getInt("generator.throttling.elements"))
    per <- Try(config.getDuration("generator.throttling.per"))
            .map(d => FiniteDuration(d.toMillis, TimeUnit.MILLISECONDS))
  } yield (elements, per)
}

object Configuration extends ExtensionId[ConfigurationImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ConfigurationImpl =
    new ConfigurationImpl(system.settings.config)

  override def lookup(): ExtensionId[_ <: Extension] = Configuration
}
