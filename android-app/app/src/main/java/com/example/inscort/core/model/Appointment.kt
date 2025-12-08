package com.example.inscort.core.model

data class Appointment(
    val id: Long = 0L,
    val courseId: Long,
    val courseTitle: String,
    val meetingName: String,
    val placeCount: Int,
    val scheduledAt: Long
)