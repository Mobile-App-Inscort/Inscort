package com.example.inscort.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object PythonRetrofitProvider {

    // 여기 포트는 FastAPI/uvicorn 포트로 맞춰줘 (예: 8000, 9000 등)
    // 에뮬레이터에서 PC의 localhost는 10.0.2.2
    private const val BASE_URL = "http://10.0.2.2:9000/"

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)   // 응답 2분까지 기다리기
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val crawlerApi: CrawlerApi by lazy {
        retrofit.create(CrawlerApi::class.java)
    }
}
