package com.mygolbs.mybus.api

import com.mygolbs.mybus.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * API 客户端 - 支持多后端切换
 * 
 * 使用方式:
 *   BusApiClient.instance.searchLines(...)  // 掌上公交 API
 *   BusApiClient.amapInstance.searchLine(...)  // 高德地图 API（备用）
 */
object BusApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Connection", "keep-alive")
                .build()
            chain.proceed(request)
        }
        .build()

    /** 掌上公交后端 API */
    val instance: BusApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BUS_API_BASE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BusApiService::class.java)
    }

    /** 高德地图公交 API（备用方案） */
    val amapInstance: AmapBusApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.AMAP_API_BASE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AmapBusApiService::class.java)
    }
}
