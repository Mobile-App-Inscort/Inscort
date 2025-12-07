package com.example.inscort.core.ocr

/**
 * 모든 OCR 구현이 공통으로 반환해야 할 결과 모델
 */
data class OcrResult(
    val imageUrl: String,      // 어떤 이미지에서 나온 텍스트인지
    val fullText: String,      // 전체 인식 텍스트
    val lines: List<String>    // 줄 단위 텍스트
)