package com.example.inscort.data.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// POST /crawl 요청 바디
data class CrawlRequest(
    val url: String
)

// 응답: images 원소
data class CrawledImage(
    val key: String,   // s3 object key
    val url: String    // presigned url
)

// 전체 응답
data class CrawlResponse(
    val postId: String,
    val images: List<CrawledImage>
)

interface CrawlerApi {

    @Headers("Content-Type: application/json")
    @POST("crawl")
    suspend fun crawl(
        @Body body: CrawlRequest
    ): CrawlResponse
}
