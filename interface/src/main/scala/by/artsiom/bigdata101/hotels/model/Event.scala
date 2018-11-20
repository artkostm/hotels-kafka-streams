package by.artsiom.bigdata101.hotels.model

import java.util.Date

final case class Event(dateTime: Date,
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
