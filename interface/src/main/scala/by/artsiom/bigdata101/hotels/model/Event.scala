package by.artsiom.bigdata101.hotels.model

import java.text.SimpleDateFormat
import java.sql.{Date, Timestamp}

import com.sksamuel.avro4s.{Decoder, Encoder, SchemaFor}
import org.apache.avro.Schema

final case class Event(dateTime: Timestamp,
                       siteName: Int,
                       posaContinent: Int,
                       userLocationCountry: Int,
                       userLocationRegion: Int,
                       userLocationCity: Int,
                       origDestinationDistance: Float,
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
  val isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'")

  implicit object DateSchemaFor extends SchemaFor[Date] {
    override val schema: Schema = Schema.create(Schema.Type.STRING)
  }

  implicit object DateEncoder extends Encoder[Date] {
    override def encode(t: Date, schema: Schema): AnyRef = isoFormatter.format(t)
  }

  implicit object DateDecoder extends Decoder[Date] {
    override def decode(value: Any, schema: Schema): Date = new Date(isoFormatter.parse(value.toString).getTime)
  }
}