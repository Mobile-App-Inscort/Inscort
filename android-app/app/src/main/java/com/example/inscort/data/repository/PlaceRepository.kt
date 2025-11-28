// data/repository/PlaceRepository.kt (DB + Python + Kakao 다 엮는 자리)
import com.example.inscort.core.model.Place
import com.example.inscort.data.local.dao.CourseDao
import com.example.inscort.data.local.dao.PlaceDao
//import com.example.inscort.data.api.PythonOcrApi
//import com.example.inscort.data.api.KakaoLocalApi
import com.example.inscort.data.mapper.toEntity
import com.example.inscort.data.mapper.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaceRepository(
    private val placeDao: PlaceDao,
    private val courseDao: CourseDao,
    //private val pythonApi: PythonOcrApi,
    //private val kakaoApi: KakaoLocalApi
) {

    fun getAllPlaces(): Flow<List<Place>> =
        placeDao.getAllPlaces().map { list -> list.map { it.toModel() } }

    suspend fun savePlace(place: Place): Long =
        placeDao.insert(place.toEntity())

    // 코스 저장, 코스+플레이스 로딩 등도 여기서 구현
}
