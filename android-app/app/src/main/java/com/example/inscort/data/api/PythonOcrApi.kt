package com.example.inscort.data.api


//import retrofit2.http.Body
//import retrofit2.http.Headers
//import retrofit2.http.POST

/**
 * Python OCR 서버와 통신하는 Retrofit 인터페이스
 * 서버는 대충 이런 형식으로 맞추면 됨:
 *  POST /ocr
 *  body: { images: [ { imageUrl, base64 } ] }
 *  response: { results: [ { imageUrl, fullText, lines } ] }
 */

data class OcrImageRequest(
    val imageUrl: String,
    val base64: String    // 또는 서버에서 URL 다운받게 할 거면 생략 가능
)

data class OcrRequestBody(
    val images: List<OcrImageRequest>
)

data class OcrResponseItem(
    val imageUrl: String,
    val fullText: String,
    val lines: List<String>
)

data class OcrResponse(
    val results: List<OcrResponseItem>
)

/*
interface OcrApi {

    @Headers("Content-Type: application/json")
    @POST("ocr")
    suspend fun recognize(
        @Body body: OcrRequestBody
    ): OcrResponse
}
*/