package com.example.inscort.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun courseDao(): CourseDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}