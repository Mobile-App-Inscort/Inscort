package com.example.inscort.ui.explore


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inscort.core.ocr.OcrService
import com.example.inscort.core.ocr.OcrResult
import com.example.inscort.core.ocr.toPlaceCandidates
import com.example.inscort.core.model.Place
import com.example.inscort.data.repository.PlaceRepository
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OcrUiState(
    val loading: Boolean = false,
    val results: List<OcrResult> = emptyList(),
    val placeSuggestions: List<Place> = emptyList(),   // ← 타입 변경
    val errorMessage: String? = null
)


class CourseDiscoveryViewModel(
    private val ocrService: OcrService,
    private val placeRepository: PlaceRepository
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
}
