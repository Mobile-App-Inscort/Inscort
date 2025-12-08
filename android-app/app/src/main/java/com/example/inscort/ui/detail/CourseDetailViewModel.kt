package com.example.inscort.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inscort.core.model.Place
import com.example.inscort.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val repository: PlaceRepository // A-2가 만든 저장소
) : ViewModel() {
    // 1. 코스 이름 (DB에서 가져올 예정)
    private val _courseTitle = MutableStateFlow("로딩 중...")
    val courseTitle = _courseTitle.asStateFlow()

    // 1. 화면에 보여줄 장소들 (마커용)
    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places = _places.asStateFlow()

    // 2. 화면에 그려줄 경로 좌표들 (선 그리기용 - ★핵심)
    private val _routePoints = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val routePoints = _routePoints.asStateFlow()

    // 초기화: DB에서 코스 정보 가져오기
    fun loadCourse(courseId: Long) {
        viewModelScope.launch {
            // (1) DB에서 장소 리스트 가져오기
            val loadedPlaces = repository.getPlacesByCourseId(courseId)
            Log.d("CourseDetailViewModel", "loadedPlaces size=${loadedPlaces.size}")
            _places.value = loadedPlaces

            // TODO: CourseEntity도 가져와서 title 채우는 로직 추가 필요
            _courseTitle.value = "나의 데이트 코스"

            // (2) 장소가 2개 이상이면 길찾기 API 호출!
            if (loadedPlaces.size >= 2) {
                try {
                    val routeData = repository.getRoute(loadedPlaces)
                    _routePoints.value = routeData
                } catch (e: Exception) {
                    Log.e("CourseDetail", "길찾기 실패: ${e.message}")
                }
            }
        }
    }
    }


