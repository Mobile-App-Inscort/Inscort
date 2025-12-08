package com.example.inscort.data.repository


import android.graphics.Bitmap
import android.util.Base64
import com.example.inscort.core.ocr.OcrResult
import com.example.inscort.core.ocr.OcrService
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Python OCR 서버를 사용하는 구현체
 * 나중에 ML Kit 대신 이걸로 갈아끼우면 됨.
 */
/*
class PythonOcrService(
    private val api: OcrApi
) : OcrService {

    override suspend fun recognizeImages(
        images: List<Pair<InputImage, String>>
    ): List<OcrResult> {
        // 여기서는 InputImage → Bitmap 변환이 필요할 수 있음
        // (실제 프로젝트에 맞게 변환 로직을 넣어줘야 함)
        val requestImages = images.map { (inputImage, url) ->
            val bitmap = inputImage.bitmapInternal
                ?: error("InputImage must be created from Bitmap to use PythonOcrService")

            val base64 = withContext(Dispatchers.Default) {
                bitmapToBase64(bitmap)
            }

            OcrImageRequest(
                imageUrl = url,
                base64 = base64
            )
        }

        val response = api.recognize(OcrRequestBody(images = requestImages))

        return response.results.map { item ->
            OcrResult(
                imageUrl = item.imageUrl,
                fullText = item.fullText,
                lines = item.lines
            )
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
*/