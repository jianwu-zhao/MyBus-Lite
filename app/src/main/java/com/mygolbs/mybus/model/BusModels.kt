package com.mygolbs.mybus.model

import com.google.gson.annotations.SerializedName

/**
 * 数据模型 - 与掌上公交后端 API 对应
 * TODO: 等 Frida 动态抓包后替换为真实字段名
 */

/** 城市 */
data class City(
    @SerializedName("cityId") val cityId: String = "",
    @SerializedName("cityName") val cityName: String = "",
    @SerializedName("province") val province: String = "",
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("pinyin") val pinyin: String = "",
    @SerializedName("hot") val hot: Boolean = false
)

/** 公交线路 */
data class BusLine(
    @SerializedName("lineId") val lineId: String = "",
    @SerializedName("lineName") val lineName: String = "",
    @SerializedName("fromName") val fromName: String = "",
    @SerializedName("toName") val toName: String = "",
    @SerializedName("startTime") val startTime: String = "",
    @SerializedName("endTime") val endTime: String = "",
    @SerializedName("price") val price: String = "",
    @SerializedName("distance") val distance: String = "",
    @SerializedName("company") val company: String = "",
    @SerializedName("direction") val direction: Int = 0
)

/** 站点 */
data class Station(
    @SerializedName("stationId") val stationId: String = "",
    @SerializedName("stationName") val stationName: String = "",
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("orderNo") val orderNo: Int = 0,
    @SerializedName("alias") val alias: String = ""
)

/** 线路详情（含站点列表） */
data class LineDetail(
    @SerializedName("line") val line: BusLine? = null,
    @SerializedName("upStations") val upStations: List<Station> = emptyList(),
    @SerializedName("downStations") val downStations: List<Station> = emptyList()
)

/** 实时公交信息 */
data class RealtimeBus(
    @SerializedName("vehicleId") val vehicleId: String = "",
    @SerializedName("plateNo") val plateNo: String = "",
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("speed") val speed: Float = 0f,
    @SerializedName("azimuth") val azimuth: Float = 0f,
    @SerializedName("distance") val distance: Int = 0,  // 到当前站距离 米
    @SerializedName("stationIndex") val stationIndex: Int = 0,  // 当前经过站点序号
    @SerializedName("nextStation") val nextStation: String = "",
    @SerializedName("arrivalTime") val arrivalTime: String = "",  // 预计到达时间
    @SerializedName("isArriving") val isArriving: Boolean = false,  // 是否即将进站
    @SerializedName("isFull") val isFull: Boolean = false
)

/** 路线规划结果 */
data class RoutePlan(
    @SerializedName("duration") val duration: Int = 0,  // 分钟
    @SerializedName("distance") val distance: Int = 0,  // 米
    @SerializedName("walkDistance") val walkDistance: Int = 0,
    @SerializedName("price") val price: Float = 0f,
    @SerializedName("segments") val segments: List<RouteSegment> = emptyList()
)

data class RouteSegment(
    @SerializedName("type") val type: String = "",  // walk/bus/subway
    @SerializedName("instruction") val instruction: String = "",
    @SerializedName("distance") val distance: Int = 0,
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("lineName") val lineName: String = "",
    @SerializedName("fromName") val fromName: String = "",
    @SerializedName("toName") val toName: String = "",
    @SerializedName("stations") val stations: List<String> = emptyList()
)

/** API 通用响应包装 */
data class ApiResponse<T>(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: T? = null
)

data class ApiListResponse<T>(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: List<T> = emptyList(),
    @SerializedName("total") val total: Int = 0
)
