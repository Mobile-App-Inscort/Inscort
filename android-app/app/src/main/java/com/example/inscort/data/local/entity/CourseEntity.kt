// data/local/entity/CourseEntity.kt
package com.example.inscort.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
