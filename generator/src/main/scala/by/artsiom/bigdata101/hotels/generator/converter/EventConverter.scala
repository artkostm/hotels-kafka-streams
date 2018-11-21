package by.artsiom.bigdata101.hotels.generator.converter

import by.artsiom.bigdata101.hotels.generator.Message
import by.artsiom.bigdata101.hotels.model.Event

object EventConverter {
  /**
    * To convert Event to Kafka message
    *
    * @param topic - the name of a topic that exists in Kafka
    * @return A Function that accepts Event and converts it to ProducerRecord
    */
  def apply(topic: String): Event => Message =
    event => new Message(topic, event.dateTime.toString)
}
