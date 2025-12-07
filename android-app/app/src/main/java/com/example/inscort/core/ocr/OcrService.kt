package com.example.inscort.core.ocr

import com.google.mlkit.vision.common.InputImage

/**
 * OCR 추상 인터페이스
 * 👉 ViewModel / Usecase는 이 인터페이스만 의존
 */
interface OcrService {

    /**
     * @param images (InputImage, imageUrl) 리스트
     * @return 각 이미지에 대한 OCR 결과 리스트
     */
    suspend fun recognizeImages(
        images: List<Pair<InputImage, String>>
    ): List<OcrResult>
}