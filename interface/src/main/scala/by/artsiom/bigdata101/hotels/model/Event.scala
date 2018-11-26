package by.artsiom.bigdata101.hotels.model

import java.sql.{Date, Timestamp}
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import com.sksamuel.avro4s.{Decoder, Encoder, SchemaFor}
import org.apache.avro.Schema

final case class Event(dateTime: Timestamp,
                       siteName: Int,
                       posaContinent: Int,
                       userLocationCountry: Int,
                       userLocationRegion: Int,
                       userLocationCity: Int,
                       origDestinationDistance: Double,
                       userId: Int,
                       isMobile: Boolean,
                       isPackage: Boolean,
                       channel: Int,
                       srchCi: Date,
                       srchCo: Date,
                       srchAdultsCnt: Int,
                       srchChildrenCnt: Int,
                       srchRmCnt: Int,
                       srchDestinationId: Int,
                       srchDestinationTypeId: Int,
                       isBooking: Boolean,
                       cnt: Int,
                       hotelContinent: Int,
                       hotelCountry: Int,
                       hotelMarket: Int,
                       hotelCluster: Int)

object Event {
  implicit object DateSchemaFor extends SchemaFor[OffsetDateTime] {
    override val schema: Schema = Schema.create(Schema.Type.STRING)
  }

  implicit object DateEncoder extends Encoder[OffsetDateTime] {
    override def encode(t: OffsetDateTime, schema: Schema): AnyRef =
      t.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }

  implicit object DateDecoder extends Decoder[OffsetDateTime] {
    override def decode(value: Any, schema: Schema): OffsetDateTime =
      OffsetDateTime.parse(value.toString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }
}