// data/local/entity/PlaceEntity.kt
package com.example.inscort.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val sourceUrl: String?
)
