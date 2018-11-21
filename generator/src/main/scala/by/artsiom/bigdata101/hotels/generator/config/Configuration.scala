package by.artsiom.bigdata101.hotels.generator.config

import akka.actor.{
  ExtendedActorSystem,
  Extension,
  ExtensionId,
  ExtensionIdProvider
}
import com.typesafe.config.Config

private[config] class ConfigurationImpl(config: Config) extends Extension {

  val NumberOfEvents = config.getInt("generator.number-of-events")
  val Parallelism = config.getInt("akka.kafka.producer.parallelism")
  val TopicName = config.getString("akka.kafka.producer.topic.name")

}

object Configuration
    extends ExtensionId[ConfigurationImpl]
    with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ConfigurationImpl =
    new ConfigurationImpl(system.settings.config)

  override def lookup(): ExtensionId[_ <: Extension] = Configuration
}
