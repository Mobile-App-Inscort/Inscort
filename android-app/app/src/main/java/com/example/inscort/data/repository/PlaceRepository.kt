package com.example.inscort.data.repository

import android.util.Log
import com.example.inscort.BuildConfig
import com.example.inscort.data.local.entity.CoursePlaceCrossRef
import com.example.inscort.core.model.Place
import com.example.inscort.data.api.KakaoNaviApi
import com.example.inscort.data.local.dao.PlaceDao
import com.example.inscort.data.local.entity.CourseEntity
import com.example.inscort.data.local.entity.PlaceEntity
import com.example.inscort.core.ocr.PlaceCandidate
import com.example.inscort.data.api.KakaoLocalApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaceRepository(
    private val placeDao: PlaceDao,
    private val naviApi: KakaoNaviApi,
    private val localApi: KakaoLocalApi,   // ← 추가
    private val kakaoApiKey: String = BuildConfig.KAKAO_REST_API_KEY
) {
    // 1. 모든 장소 가져오기 (에러 났던 부분 해결)
    suspend fun getAllPlaces(): List<Place> = withContext(Dispatchers.IO) {
        // [해결] map 뒤에 { entity -> ... } 를 명시해서 타입 추론 에러 방지
        return@withContext placeDao.getAllPlaces().map { entity ->
            Place(
                id = entity.id,
                name = entity.name,
                address = entity.address,
                latitude = entity.latitude,
                longitude = entity.longitude,
                sourceUrl = null
            )
        }
    }

    suspend fun insertCourse(course: CourseEntity): Long = withContext(Dispatchers.IO) {
        return@withContext placeDao.insertCourse(course)
    }

    suspend fun insertCoursePlace(crossRef: CoursePlaceCrossRef) = withContext(Dispatchers.IO) {
        placeDao.insertCoursePlace(crossRef)
    }

    // 2. 코스 ID로 장소 가져오기
    suspend fun getPlacesByCourseId(courseId: Long): List<Place> = withContext(Dispatchers.IO) {
        return@withContext placeDao.getPlacesByCourseId(courseId).map { entity ->
            Place(
                id = entity.id,
                name = entity.name,
                address = entity.address,
                latitude = entity.latitude,
                longitude = entity.longitude,
                sourceUrl = null
            )
        }
    }

    // ▼▼▼ [추가] 장소를 DB에 저장하는 함수 ▼▼▼
    suspend fun insertPlace(place: Place) = withContext(Dispatchers.IO) {
        // UI용 모델(Place) -> DB용 모델(PlaceEntity) 변환
        val entity = PlaceEntity(
            id = place.id, // (Entity의 PK 이름이 id면 id로 수정하세요)
            name = place.name,
            address = place.address,
            latitude = place.latitude,
            longitude = place.longitude,
            sourceUrl = place.sourceUrl
            // 필요한 필드 다 채우기
        )
        placeDao.insertPlace(entity)
    }

    // 3. 길찾기 경로 가져오기 (기존 코드 유지)
    suspend fun getRoute(places: List<Place>): List<Pair<Double, Double>> = withContext(Dispatchers.IO) {
        if (places.size < 2) return@withContext emptyList()

        try {
            val origin = "${places.first().longitude},${places.first().latitude}"
            val destination = "${places.last().longitude},${places.last().latitude}"

            val waypoints = if (places.size > 2) {
                places.subList(1, places.size - 1).joinToString("|") { place ->
                    "${place.longitude},${place.latitude}"
                }
            } else null

            val response = naviApi.getDirections(
                apiKey = "KakaoAK \$kakaoApiKey",
                origin = origin,
                destination = destination,
                waypoints = waypoints
            )

            val linePoints = mutableListOf<Pair<Double, Double>>()

            response.routes.forEach { route ->
                route.sections.forEach { section ->
                    section.roads.forEach { road ->
                        val rawCoords = road.vertexes
                        for (i in rawCoords.indices step 2) {
                            linePoints.add(Pair(rawCoords[i + 1], rawCoords[i]))
                        }
                    }
                }
            }
            return@withContext linePoints

        } catch (e: Exception) {
            Log.e("PlaceRepository", "Error: ${e.message}")
            return@withContext emptyList()
        }
    }

    suspend fun searchPlacesFromCandidates(
        candidates: List<PlaceCandidate>
    ): List<Place> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Place>()

        for (candidate in candidates) {
            val response = localApi.searchKeyword(
                auth = "KakaoAK $kakaoApiKey",
                query = candidate.query,
                size = 1
            )

            val doc = response.documents.firstOrNull() ?: continue
            Log.d("PlaceRepository", "query=${candidate.query}, result=${doc.place_name}")

            result += Place(
                id = 0L, // 아직 DB에 없는 애들이라 0L 같은 더미 ID
                name = doc.place_name,
                address = doc.road_address_name?.takeIf { it.isNotEmpty() } ?: doc.address_name,
                latitude = doc.y.toDouble(),
                longitude = doc.x.toDouble(),
                sourceUrl = null   // 필요하면 place_url 필드 추가해서 넣기
            )
        }
        result
    }

}
