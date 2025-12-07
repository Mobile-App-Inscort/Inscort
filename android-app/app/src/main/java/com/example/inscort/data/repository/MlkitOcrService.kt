package com.example.inscort.data.repository


import com.example.inscort.core.ocr.OcrResult
import com.example.inscort.core.ocr.OcrService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * ML Kit 기반 on-device OCR 구현체
 */
class MlKitOcrService : OcrService {

    // 한글 인식기 (다국어 쓰고 싶으면 옵션 바꾸면 됨)
    private val recognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    override suspend fun recognizeImages(
        images: List<Pair<InputImage, String>>
    ): List<OcrResult> = coroutineScope {

        // 이미지들 병렬 처리
        images.map { (image, url) ->
            async(Dispatchers.IO) {
                val visionText = recognizer.process(image).await()

                val lines = visionText.textBlocks
                    .flatMap { it.lines }
                    .map { it.text }

                OcrResult(
                    imageUrl = url,
                    fullText = visionText.text,
                    lines = lines
                )
            }
        }.awaitAll()
    }
}
