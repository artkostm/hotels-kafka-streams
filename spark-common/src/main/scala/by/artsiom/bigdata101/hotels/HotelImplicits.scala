package by.artsiom.bigdata101.hotels
import java.io.ByteArrayInputStream

import by.artsiom.bigdata101.hotels.model.Event
import com.sksamuel.avro4s.{AvroInputStream, AvroSchema}
import org.apache.avro.Schema
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.util.LongAccumulator

trait HotelImplicits extends Serializable {
  implicit class HotelsDataframeDeserializer(dataframe: DataFrame)(implicit spark: SparkSession)
      extends Serializable {
    import spark.implicits._

    val rowEventSchema = spark.sparkContext.broadcast(AvroSchema[Event].toString)

    def fromAvro(
      columnToParse: String,
      acc: LongAccumulator = spark.sparkContext.longAccumulator("events_count")
    ): Dataset[Event] =
      dataframe.map { row =>
        val bais = new ByteArrayInputStream(row.getAs[Array[Byte]](columnToParse))
        val input =
          AvroInputStream
            .binary[Event]
            .from(bais)
            .build(new Schema.Parser().parse(rowEventSchema.value))
        acc.add(1)
        input.iterator.next()
      }
  }
}
