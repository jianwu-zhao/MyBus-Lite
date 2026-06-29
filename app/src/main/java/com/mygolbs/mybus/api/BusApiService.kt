package com.mygolbs.mybus.api

import com.mygolbs.mybus.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 掌上公交后端 API 接口定义
 * 
 * 注意：这些接口路径是推断的，等 Frida 动态抓包后需要修正。
 * 目前基于常见公交 API 模式 + strings.xml 中的 URL 推断。
 * 
 * 原始 URL: http://117.40.140.76:8084/hctsm-server/aapp
 */
interface BusApiService {

    /** 获取支持的城市列表 */
    @GET("city/list")
    suspend fun getCities(): Response<ApiListResponse<City>>

    /** 搜索城市 */
    @GET("city/search")
    suspend fun searchCities(@Query("keyword") keyword: String): Response<ApiListResponse<City>>

    /** 按关键词搜索线路 */
    @GET("line/search")
    suspend fun searchLines(
        @Query("cityId") cityId: String,
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1
    ): Response<ApiListResponse<BusLine>>

    /** 获取线路详情 */
    @GET("line/detail")
    suspend fun getLineDetail(
        @Query("lineId") lineId: String,
        @Query("cityId") cityId: String
    ): Response<ApiResponse<LineDetail>>

    /** 获取线路实时公交位置 */
    @GET("realtime/buses")
    suspend fun getRealtimeBuses(
        @Query("lineId") lineId: String,
        @Query("cityId") cityId: String,
        @Query("direction") direction: Int = 0
    ): Response<ApiListResponse<RealtimeBus>>

    /** 获取附近站点 */
    @GET("station/nearby")
    suspend fun getNearbyStations(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int = 500
    ): Response<ApiListResponse<Station>>

    /** 获取经过站点的线路 */
    @GET("station/lines")
    suspend fun getStationLines(
        @Query("stationId") stationId: String,
        @Query("cityId") cityId: String
    ): Response<ApiListResponse<BusLine>>

    /** 路线规划 */
    @GET("route/plan")
    suspend fun routePlan(
        @Query("fromLat") fromLat: Double,
        @Query("fromLng") fromLng: Double,
        @Query("toLat") toLat: Double,
        @Query("toLng") toLng: Double,
        @Query("cityId") cityId: String
    ): Response<ApiResponse<RoutePlan>>
}
