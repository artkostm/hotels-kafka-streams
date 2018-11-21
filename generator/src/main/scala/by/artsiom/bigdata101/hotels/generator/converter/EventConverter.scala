package by.artsiom.bigdata101.hotels.generator.converter

import by.artsiom.bigdata101.hotels.generator.Message
import by.artsiom.bigdata101.hotels.model.Event

object EventConverter {
  def apply(topic: String): Event => Message =
    event => new Message(topic, event.dateTime.toString)
}
