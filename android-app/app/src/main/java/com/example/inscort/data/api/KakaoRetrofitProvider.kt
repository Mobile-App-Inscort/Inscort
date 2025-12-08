package com.example.inscort.data.api

import com.example.inscort.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KakaoRetrofitProvider {

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY   // 요청/응답 바디까지 Logcat에 찍힘
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/")   // Kakao REST base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val localApi: KakaoLocalApi by lazy {
        retrofit.create(KakaoLocalApi::class.java)
    }

    // (있으면) 내비 API도 같이
     val naviApi: KakaoNaviApi by lazy { retrofit.create(KakaoNaviApi::class.java) }
}
