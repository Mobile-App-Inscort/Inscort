// data/local/dao/PlaceDao.kt
package com.example.inscort.data.local.dao

import androidx.room.*
import com.example.inscort.data.local.entity.CoursePlaceCrossRef
import com.example.inscort.data.local.entity.CourseEntity
import com.example.inscort.data.local.entity.PlaceEntity

@Dao
interface PlaceDao {
    // 코스 ID에 해당하는 장소들을 순서대로 가져오는 쿼리
    // (CoursePlaceCrossRef 테이블과 조인해서 가져옴)
    @Query("""
        SELECT places.* FROM places 
        INNER JOIN course_place_cross_ref 
        ON places.id = course_place_cross_ref.placeId 
        WHERE course_place_cross_ref.courseId = :courseId 
        ORDER BY course_place_cross_ref.orderIndex ASC
    """)
    suspend fun getPlacesByCourseId(courseId: Long): List<PlaceEntity>

    @Query("SELECT * FROM places")
    suspend fun getAllPlaces(): List<PlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    // 코스-장소 연결 정보 저장
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoursePlace(crossRef: CoursePlaceCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // 이미 있는 장소면 덮어쓰기
    suspend fun insertPlace(place: PlaceEntity)
}
