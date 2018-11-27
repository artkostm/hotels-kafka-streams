package by.artsiom.bigdata101.hotels.generator.publisher

import java.sql.{Date, Timestamp}
import java.time.{Instant, OffsetDateTime, ZoneOffset, LocalDateTime => LDT}

import org.scalacheck.Arbitrary
import org.scalacheck.Gen._

trait GenUtils {
  val SECONDS_PER_HOUR = 3600
  val MAX_SECONDS      = 18 * SECONDS_PER_HOUR

  import ZoneOffset._

  implicit lazy val arbOffsetDateTime: Arbitrary[OffsetDateTime] =
    Arbitrary {
      for {
        seconds    <- chooseNum(LDT.MIN.toEpochSecond(UTC), LDT.MAX.toEpochSecond(UTC))
        nanos      <- chooseNum(LDT.MIN.getNano, LDT.MAX.getNano)
        zoneOffset <- chooseNum(-MAX_SECONDS, MAX_SECONDS)
      } yield
        OffsetDateTime.of(
          LDT.ofEpochSecond(seconds, nanos, UTC),
          ofTotalSeconds(zoneOffset)
        )
    }

  implicit lazy val arbDate: Arbitrary[Date] =
    Arbitrary {
      for {
        sec  <- chooseNum(-3600, 3600)
      } yield new Date(Instant.now().plusSeconds(sec).toEpochMilli)
    }

  implicit lazy val arbTimestamp: Arbitrary[Timestamp] =
    Arbitrary {
      for {
        sec  <- chooseNum(-3600, 3600)
      } yield new Timestamp(Instant.now().plusSeconds(sec).toEpochMilli)
    }
}
