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

    // 1. 화면에 보여줄 장소들 (마커용)
    private val _coursePlaces = MutableStateFlow<List<Place>>(emptyList())
    val coursePlaces = _coursePlaces.asStateFlow()

    // 2. 화면에 그려줄 경로 좌표들 (선 그리기용 - ★핵심)
    private val _routePoints = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val routePoints = _routePoints.asStateFlow()

    // 초기화: DB에서 코스 정보 가져오기
    fun loadCourse(courseId: Long) {
        viewModelScope.launch {
            val places = repository.getPlacesByCourseId(courseId)
            _coursePlaces.value = places

            // 장소가 2개 이상이면 바로 길찾기 API 호출!
            if (places.size >= 2) {
                getRouteData(places)
            }
        }
    }

    // ★ [해결] 이 함수가 없어서 에러났던 것!
    fun getRouteData(places: List<Place>) {
        viewModelScope.launch {
            // Repository(백엔드 역할)에게 경로 데이터 요청
            val routeData = repository.getRoute(places)
            _routePoints.value = routeData
        }
    }
}