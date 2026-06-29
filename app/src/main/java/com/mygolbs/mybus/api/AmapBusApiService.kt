package com.mygolbs.mybus.api

import com.mygolbs.mybus.BuildConfig
import retrofit2.Response
import retrofit2.http.*

/**
 * 高德地图公交 API - 备用数据源
 * 
 * 文档: https://lbs.amap.com/api/webservice/guide/api/transpress
 * 注意：此 key 是网页端 key，可能需要替换为服务端 key
 */
interface AmapBusApiService {

    /** 公交线路查询 */
    @GET("bus/linename")
    suspend fun searchLine(
        @Query("key") key: String = BuildConfig.AMAP_KEY,
        @Query("keywords") keywords: String,
        @Query("city") city: String,
        @Query("offset") offset: Int = 10,
        @Query("page") page: Int = 1,
        @Query("extensions") extensions: String = "all"
    ): Response<AmapBusResponse>

    /** 公交站点查询 */
    @GET("bus/stationname")
    suspend fun searchStation(
        @Query("key") key: String = BuildConfig.AMAP_KEY,
        @Query("keywords") keywords: String,
        @Query("city") city: String,
        @Query("offset") offset: Int = 10,
        @Query("page") page: Int = 1
    ): Response<AmapStationResponse>

    /** 公交路径规划 */
    @GET("direction/transit/integrated")
    suspend fun routePlan(
        @Query("key") key: String = BuildConfig.AMAP_KEY,
        @Query("origin") origin: String,  // "lng,lat"
        @Query("destination") destination: String,
        @Query("city") city: String,
        @Query("cityd") cityd: String = city,
        @Query("strategy") strategy: Int = 0
    ): Response<AmapRouteResponse>
}

/** 高德 API 通用响应 */
data class AmapBaseResponse(
    val status: String = "0",
    val info: String = "",
    val infocode: String = "",
    val count: String = "0"
)

data class AmapBusResponse(
    val status: String = "0",
    val info: String = "",
    val buslines: List<AmapBusLine> = emptyList()
)

data class AmapBusLine(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val start_stop: String = "",
    val end_stop: String = "",
    val start_time: String = "",
    val end_time: String = "",
    val company: String = "",
    val distance: String = "",
    val basic_price: String = "",
    val total_price: String = "",
    val polyline: String = "",
    val busstops: List<AmapBusStop> = emptyList()
)

data class AmapBusStop(
    val id: String = "",
    val name: String = "",
    val location: String = "",  // "lng,lat"
    val sequence: Int = 0
)

data class AmapStationResponse(
    val status: String = "0",
    val info: String = "",
    val busstops: List<AmapStation> = emptyList()
)

data class AmapStation(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val buslines: List<AmapBusLineBrief> = emptyList()
)

data class AmapBusLineBrief(
    val id: String = "",
    val name: String = "",
    val type: String = ""
)

data class AmapRouteResponse(
    val status: String = "0",
    val info: String = "",
    val route: AmapRoute? = null
)

data class AmapRoute(
    val transit: AmapTransit? = null
)

data class AmapTransit(
    val segments: List<AmapSegment> = emptyList()
)

data class AmapSegment(
    val walking: AmapWalking? = null,
    val bus: AmapBusSegment? = null,
    val railway: AmapRailway? = null
)

data class AmapWalking(
    val origin: String = "",
    val destination: String = "",
    val distance: String = "",
    val steps: List<AmapWalkStep> = emptyList()
)

data class AmapWalkStep(
    val instruction: String = "",
    val road: String = ""
)

data class AmapBusSegment(
    val buslines: List<AmapTransitBusLine> = emptyList()
)

data class AmapTransitBusLine(
    val name: String = "",
    val id: String = "",
    val type: String = "",
    val departure_stop: AmapStop = AmapStop(),
    val arrival_stop: AmapStop = AmapStop(),
    val distance: String = "",
    val duration: String = ""
)

data class AmapStop(
    val name: String = "",
    val location: String = "",
    val id: String = ""
)

data class AmapRailway(
    val name: String = "",
    val distance: String = "",
    val time: String = ""
)
