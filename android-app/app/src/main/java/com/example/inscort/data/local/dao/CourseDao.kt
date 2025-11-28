// data/local/dao/CourseDao.kt
package com.example.inscort.data.local.dao

import androidx.room.*
import com.example.inscort.data.local.entity.*

import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoursePlaceCrossRef(crossRef: CoursePlaceCrossRef)

    @Transaction
    @Query("SELECT * FROM courses")
    fun getCoursesWithPlaces(): Flow<List<CourseWithPlaces>>
}
