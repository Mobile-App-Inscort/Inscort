package com.example.inscort.ui.explore

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.inscort.core.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExploreViewModel : ViewModel() {

    // 전체 장소 목록 (분석 결과)
    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places.asStateFlow()

    // [추가] 사용자가 선택(체크)한 장소들
    // mutableStateListOf를 쓰면 UI가 즉시 반응합니다.
    private val _selectedPlaces = mutableStateListOf<Place>()
    val selectedPlaces: List<Place> get() = _selectedPlaces
    init {
        onAnalyzeLink("https://instagram.com/test") // 가짜 데이터 바로 로딩
    }
    fun onAnalyzeLink(url: String) {
        // (기존 가짜 데이터 로직 유지...)
        val fakeResult = listOf(
            Place(1L, "스타벅스 강남역점", "서울 강남구 강남대로 지하 396", 37.4979, 127.0276, url),
            Place(2L, "텐동 아사히야", "서울 강남구 테헤란로 131", 37.5000, 127.0300, url),
            Place(3L, "디저트39 성수점", "서울 성동구 연무장길 65", 37.5400, 127.0500, url)
        )
        _places.value = fakeResult
        _selectedPlaces.clear() // 초기화
    }

    // [추가] 장소 선택 토글 (체크/해제)
    fun toggleSelection(place: Place) {
        if (_selectedPlaces.contains(place)) {
            _selectedPlaces.remove(place)
        } else {
            _selectedPlaces.add(place)
        }
    }
}