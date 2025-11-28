// data/local/entity/CoursePlaceCrossRef.kt
package com.example.inscort.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "course_place_cross_ref",
    primaryKeys = ["courseId", "placeId"]
)
data class CoursePlaceCrossRef(
    val courseId: Long,
    val placeId: Long
)
