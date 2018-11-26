package by.artsiom.bigdata101.hotels
import java.io.ByteArrayInputStream

import by.artsiom.bigdata101.hotels.model.Event
import com.sksamuel.avro4s.{AvroInputStream, AvroSchema}
import org.apache.avro.Schema
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.util.LongAccumulator

trait HotelsJob {
  def streaming(topic: String, )(implicit spark: SparkSession) = {

  }
}

object HotelsJob {
  implicit class HotelsDataframeDeserializer(dataframe: Dataset[Row])(implicit spark: SparkSession) {
    import spark.implicits._

    val rowEventSchema = AvroSchema[Event].toString

    def fromAvro(columnToParse: String, acc: LongAccumulator = spark.sparkContext.longAccumulator("events_count")): Dataset[Event] =
      dataframe.map {row =>
        val bais = new ByteArrayInputStream(row.getAs[Array[Byte]](columnToParse))
        val input = AvroInputStream.binary[Event].from(bais).build(new Schema.Parser().parse(rowEventSchema))
        acc.add(1)
        input.iterator.next()
      }
  }
}
