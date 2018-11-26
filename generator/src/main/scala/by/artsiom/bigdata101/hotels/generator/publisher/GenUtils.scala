package by.artsiom.bigdata101.hotels.generator.publisher

import java.sql.{Date, Timestamp}
import java.time.{OffsetDateTime, ZoneOffset, LocalDateTime => LDT}

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
        year  <- chooseNum(0, 8099)
        month <- chooseNum(0, 11)
        day   <- chooseNum(1, 30)
      } yield new Date(year, month, day)
    }

  implicit lazy val arbTimestamp: Arbitrary[Timestamp] =
    Arbitrary {
      for {
        year  <- chooseNum(0, 8099)
        month <- chooseNum(0, 11)
        day   <- chooseNum(1, 30)
        hour  <- chooseNum(0, 23)
        min   <- chooseNum(0, 59)
        sec   <- chooseNum(0, 59)
        nano  <- chooseNum(0, 999999999)
      } yield new Timestamp(year, month, day, hour, min, sec, nano)
    }
}
