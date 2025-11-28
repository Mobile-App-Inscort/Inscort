// data/local/entity/CourseWithPlaces.kt
package com.example.inscort.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CourseWithPlaces(
    @Embedded val course: CourseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CoursePlaceCrossRef::class,
            parentColumn = "courseId",
            entityColumn = "placeId"
        )
    )
    val places: List<PlaceEntity>
)
