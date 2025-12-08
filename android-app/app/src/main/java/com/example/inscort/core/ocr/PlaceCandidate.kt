package com.example.inscort.core.ocr

/**
 * OCR 결과에서 "장소 같아 보이는 문자열"을 뽑은 1차 후보 모델
 */
data class PlaceCandidate(
    val query: String,            // Kakao Local 에 던질 검색어
    val sourceImageUrl: String    // 어떤 이미지에서 나온 줄인지
)

// 숫자만 있는 줄
private val numberOnlyRegex = Regex("^[0-9]+$")

// 기호/구두점만 있는 줄 (이모지, 느낌표 줄 등)
private val emojiOrSymbolRegex = Regex("^[\\p{P}\\p{S}]+$")

// 주소/장소 느낌 나는 패턴들
//  - 시/군/구/동/읍/면/리는 뒤에 공백이 붙는 경우만 (ex. "고양시 ")
//  - 길/로/대로/번길/로길/역/점 등은 그냥 포함만 돼도 인정
private val addressKeywordRegex = Regex(
    "(시 |군 |구 |동 |읍 |면 |리 |길|로|대로|번길|로길|역|점)"
)

// 주요 지역명 (서울/부산/…)
private val regionKeywordRegex = Regex(
    "서울|Seoul|부산|Busan|대구|광주|인천|대전|울산|세종|경기|강원|충북|충남|전북|전남|경북|경남|제주"
)

// 업종 느낌 나는 단어들
private val placeKeywordRegex = Regex(
    "카페|맛집|식당|레스토랑|호텔|게스트하우스|펍|bar|BAR|포차|분식|베이커리|빵집|브런치"
)

/**
 * OCR 한 장의 결과에서 Kakao Local 에 던질 검색 쿼리 후보들을 뽑는다.
 */
fun OcrResult.toPlaceCandidates(
    maxPerImage: Int = 12
): List<PlaceCandidate> {
    return lines
        .map { it.trim() }
        .filter { it.isNotBlank() }
        // 해시태그 / 아이디 / 링크 제거
        .filterNot { it.startsWith("#") || it.startsWith("@") || it.startsWith("http") }
        // 순수 숫자 / 기호 줄 제거
        .filterNot { it.matches(numberOnlyRegex) || it.matches(emojiOrSymbolRegex) }
        // 너무 짧거나 너무 긴 줄은 제외
        .filter { it.length in 2..25 }
        // 📌 진짜 '장소/주소처럼' 보이는 줄만 남기기
        .filter { line ->
            addressKeywordRegex.containsMatchIn(line) ||
                    regionKeywordRegex.containsMatchIn(line) ||
                    placeKeywordRegex.containsMatchIn(line)
        }
        .distinct()
        .take(maxPerImage) // 한 이미지당 최대 N개만 사용
        .map { line ->
            PlaceCandidate(
                query = line,
                sourceImageUrl = imageUrl
            )
        }
}
