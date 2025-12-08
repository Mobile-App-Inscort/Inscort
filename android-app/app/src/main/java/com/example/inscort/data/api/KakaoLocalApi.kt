package com.example.inscort.data.api


import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// Kakao Local keyword 검색 응답 DTO (필요한 필드만)
data class KakaoPlaceDocument(
    val place_name: String,
    val address_name: String,
    val road_address_name: String?,
    val x: String,  // longitude
    val y: String,   // latitude
    val place_url: String
)

data class KakaoSearchResponse(
    val documents: List<KakaoPlaceDocument>
)

interface KakaoLocalApi {

    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Header("Authorization") auth: String,   // "KakaoAK {REST_API_KEY}"
        @Query("query") query: String,
        @Query("size") size: Int = 10
    ): KakaoSearchResponse
}
