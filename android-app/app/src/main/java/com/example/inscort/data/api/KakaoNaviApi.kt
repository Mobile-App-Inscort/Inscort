package com.example.inscort.data.api

import com.example.inscort.core.model.NaviResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoNaviApi {
    @GET("v1/directions")
    suspend fun getDirections(
        @Header("Authorization") apiKey: String = "KakaoAK YOUR_REST_API_KEY", // 실제 키는 상수로 관리 권장
        @Query("origin") origin: String,       // "127.123,37.123"
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String? = null,
        @Query("priority") priority: String = "RECOMMEND" // 추천경로
    ): NaviResponse

    companion object {
        private const val BASE_URL = "https://apis-navi.kakaomobility.com/"

        fun create(): KakaoNaviApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(KakaoNaviApi::class.java)
        }
    }
}
