package com.example.inscort.data.repository

import android.util.Log
import com.example.inscort.data.local.entity.CoursePlaceCrossRef
import com.example.inscort.core.model.Place
import com.example.inscort.data.api.KakaoNaviApi
import com.example.inscort.data.local.dao.PlaceDao
import com.example.inscort.data.local.entity.CourseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaceRepository(
    private val placeDao: PlaceDao,
    private val naviApi: KakaoNaviApi
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
                apiKey = "KakaoAK YOUR_API_KEY",
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
}