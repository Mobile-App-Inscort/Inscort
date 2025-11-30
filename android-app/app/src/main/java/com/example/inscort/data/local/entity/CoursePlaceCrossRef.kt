// data/local/entity/CoursePlaceCrossRef.kt
package com.example.inscort.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "course_place_cross_ref",
    primaryKeys = ["courseId", "placeId"],
    indices = [Index(value = ["placeId"])]
)
data class CoursePlaceCrossRef(
    val courseId: Long,
    val placeId: Long,
    val orderIndex: Int,
    val memo: String? = null
)
