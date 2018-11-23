package by.artsiom.bigdata101.hotels.generator.converter

import java.io.ByteArrayOutputStream
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import by.artsiom.bigdata101.hotels.generator.Message
import by.artsiom.bigdata101.hotels.model.Event
import com.sksamuel.avro4s._
import org.apache.avro.Schema

object EventConverter {
  implicit object DateSchemaFor extends SchemaFor[OffsetDateTime] {
    override val schema: Schema = Schema.create(Schema.Type.STRING)
  }

  implicit object DateEncoder extends Encoder[OffsetDateTime] {
    override def encode(t: OffsetDateTime, schema: Schema): AnyRef =
      t.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }

  implicit private val schemaFor      = SchemaFor[Event]
  implicit private val encoder        = Encoder[Event]
  implicit private val namingStrategy = SnakeCase
  implicit private val schema         = AvroSchema[Event]

  /**
   * To convert Event to Kafka message
   *
   * @param topic - the name of a topic that exists in Kafka
   * @return A Function that accepts Event and converts it to ProducerRecord
   */
  def apply(topic: String): Event => Message = { event =>
    val baos   = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[Event].to(baos).build(schema)
    output.write(event)
    output.close()
    new Message(topic, baos.toByteArray)
  }
}
