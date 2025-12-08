package com.example.inscort.ui.explore

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inscort.core.ocr.OcrResult
import com.example.inscort.core.ocr.OcrService
import com.example.inscort.core.ocr.toPlaceCandidates
import com.example.inscort.core.model.Place
import com.example.inscort.data.api.CrawlRequest
import com.example.inscort.data.api.CrawlerApi
import com.example.inscort.data.api.PythonRetrofitProvider
import com.example.inscort.data.repository.PlaceRepository
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

enum class AnalysisStep(val order: Int) {
    Idle(0),
    CrawlingImages(1),
    Ocr(2),
    PlaceParsing(3),
    Mapping(4),
    Done(5)
}
data class OcrUiState(
    val loading: Boolean = false,
    val results: List<OcrResult> = emptyList(),
    val placeSuggestions: List<Place> = emptyList(),   // ← 타입 변경
    val errorMessage: String? = null,
    val step: AnalysisStep = AnalysisStep.Idle,
    val progress: Int = 0
)


class CourseDiscoveryViewModel(
    private val ocrService: OcrService,
    private val placeRepository: PlaceRepository,
    private val crawlerApi: CrawlerApi = PythonRetrofitProvider.crawlerApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState

    fun runOcr(images: List<Pair<InputImage, String>>) {
        _uiState.value = _uiState.value.copy(
            loading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // 1) OCR 실행
                val results = ocrService.recognizeImages(images)

                // 2) OCR 결과에서 장소 후보 텍스트 추출
                val candidates = results.flatMap { it.toPlaceCandidates() }

                // 3) Kakao Local로 장소 검색
                val suggestions = placeRepository.searchPlacesFromCandidates(candidates)

                // 4) UI 상태 업데이트
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    results = results,
                    placeSuggestions = suggestions
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = e.message ?: "OCR / 장소 검색 실패"
                )
            }
        }
    }
    fun crawlAndRunOcr(instagramUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loading = true,
                errorMessage = null,
                step = AnalysisStep.CrawlingImages,
                progress = 10
            )

            try {
                // 1) Python 서버 /crawl 호출
                val crawlResponse = crawlerApi.crawl(CrawlRequest(instagramUrl))

                _uiState.value = _uiState.value.copy(
                    step = AnalysisStep.Ocr,
                    progress = 40
                )

                // 2) presigned URL → Bitmap → InputImage 변환
                val imagePairs: List<Pair<InputImage, String>> =
                    withContext(Dispatchers.IO) {
                        crawlResponse.images.map { img ->
                            val bitmap = downloadBitmap(img.url)
                            val inputImage = InputImage.fromBitmap(bitmap, 0)
                            inputImage to img.url
                        }
                    }

                // 3) ML Kit OCR 수행
                val ocrResults = ocrService.recognizeImages(imagePairs)

                _uiState.value = _uiState.value.copy(
                    step = AnalysisStep.PlaceParsing,
                    progress = 70
                )

                // 4) 텍스트에서 장소 후보 추출 후 Kakao 검색
                val candidates = ocrResults.flatMap { it.toPlaceCandidates() }
                val suggestions = placeRepository.searchPlacesFromCandidates(candidates)

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    results = ocrResults,
                    placeSuggestions = suggestions,
                    step = AnalysisStep.Done,
                    progress = 100
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = e.message ?: "크롤링 / OCR 실패",
                    step = AnalysisStep.Idle,
                    progress = 0
                )
            }
        }
    }

    // URL → Bitmap 간단 유틸
    private suspend fun downloadBitmap(url: String): Bitmap =
        withContext(Dispatchers.IO) {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.inputStream.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }
}