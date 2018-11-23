package by.artsiom.bigdata101.hotels.generator.publisher

import java.time.{LocalDateTime => LDT, OffsetDateTime, ZoneOffset}

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
}
