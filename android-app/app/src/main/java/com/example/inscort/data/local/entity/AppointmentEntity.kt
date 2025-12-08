package com.example.inscort.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val courseId: Long,
    val courseTitle: String,
    val meetingName: String,
    val placeCount: Int,
    val scheduledAt: Long
)