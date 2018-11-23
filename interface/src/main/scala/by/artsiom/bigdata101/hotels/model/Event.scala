package by.artsiom.bigdata101.hotels.model

import java.time.OffsetDateTime

final case class Event(dateTime: OffsetDateTime,
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
                       srchCi: OffsetDateTime,
                       srchCo: OffsetDateTime,
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
