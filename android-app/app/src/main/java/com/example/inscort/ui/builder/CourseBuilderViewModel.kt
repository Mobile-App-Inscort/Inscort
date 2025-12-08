package com.example.inscort.ui.builder

import androidx.compose.runtime.getValue     // [필수] 이거 없으면 에러남
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue     // [필수] 이거 없으면 에러남
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inscort.core.model.Place
import com.example.inscort.data.local.entity.CourseEntity
import com.example.inscort.data.local.entity.CoursePlaceCrossRef
import com.example.inscort.data.repository.PlaceRepository
import kotlinx.coroutines.launch
import java.util.Collections

class CourseBuilderViewModel(
    private val repository: PlaceRepository
) : ViewModel() {

    private val _places = mutableStateListOf<Place>()
    val places: List<Place> get() = _places

    var courseTitle by mutableStateOf("")
    val isSaveEnabled: Boolean
        get() = courseTitle.isNotBlank() && _places.isNotEmpty()
    fun initPlaces(selectedPlaces: List<Place>) {
        _places.clear()
        _places.addAll(selectedPlaces)
    }

    // 순서 변경 로직 (드래그 시 호출)
    fun moveItem(fromIndex: Int, toIndex: Int) {
        // 리스트 내에서 아이템 위치 맞교환
        if (fromIndex != toIndex) {
            val item = _places.removeAt(fromIndex)
            _places.add(toIndex, item)
        }
    }
    fun updateMemo(index: Int, text: String) {
        // 메모 업데이트 로직 (간단히 구현)
        if (index in _places.indices) {
            // 실제 앱에서는 Place 객체 안에 memo 필드를 var로 만들거나 copy()를 써야 함
            // _places[index] = _places[index].copy(memo = text)
        }
    }

    fun saveCourse(onSuccess: (Long) -> Unit) {
        if (!isSaveEnabled) return // 방어 코드

        viewModelScope.launch {
            val newCourseId = repository.insertCourse(
                CourseEntity(title = courseTitle)
            )

            places.forEachIndexed { index, place ->
                val placeId = repository.insertPlace(place)
                repository.insertCoursePlace(
                    CoursePlaceCrossRef(
                        courseId = newCourseId,
                        placeId = placeId,
                        orderIndex = index, // 변경된 순서대로 저장됨
                        memo = null
                    )
                )
            }
            onSuccess(newCourseId)
        }
    }
}