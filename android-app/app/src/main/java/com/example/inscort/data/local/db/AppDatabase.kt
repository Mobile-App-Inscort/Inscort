package com.example.inscort.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.inscort.data.local.dao.CourseDao
import com.example.inscort.data.local.dao.PlaceDao
import com.example.inscort.data.local.entity.CourseEntity
import com.example.inscort.data.local.entity.CoursePlaceCrossRef
import com.example.inscort.data.local.entity.PlaceEntity

@Database(
    entities = [
        PlaceEntity::class,
        CourseEntity::class,
        CoursePlaceCrossRef::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun courseDao(): CourseDao
}