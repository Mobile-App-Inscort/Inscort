package com.example.inscort.core.ocr

/**
 * OCR 결과에서 "장소 같아 보이는 문자열"을 뽑은 1차 후보 모델
 */
data class PlaceCandidate(
    val query: String,        // Kakao Local 에 던질 검색어
    val sourceImageUrl: String   // 어떤 이미지에서 나온 줄인지
)

// 숫자만 있는 줄
private val numberOnlyRegex = Regex("^[0-9]+$")

// 기호/구두점만 있는 줄 (이모지, 느낌표 줄 등)
private val emojiOrSymbolRegex = Regex("^[\\p{P}\\p{S}]+$")

/**
 * OcrResult -> 장소 텍스트 후보 리스트로 변환
 */
fun OcrResult.toPlaceCandidates(
    maxPerImage: Int = 10
): List<PlaceCandidate> {
    return lines
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        // 해시태그 / 아이디 / 링크 제거
        .filterNot { it.startsWith("#") || it.startsWith("@") || it.startsWith("http") }
        // 순수 숫자 / 기호 줄 제거
        .filterNot { it.matches(numberOnlyRegex) || it.matches(emojiOrSymbolRegex) }
        // 너무 짧거나 너무 긴 줄은 제외 (필요하면 조절)
        .filter { it.length in 2..25 }
        .distinct()
        .take(maxPerImage) // 한 이미지당 최대 N개만 사용
        .map { line ->
            PlaceCandidate(
                query = line,
                sourceImageUrl = imageUrl
            )
        }
}
