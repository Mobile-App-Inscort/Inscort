package com.example.inscort.core.model

data class Course(
    val id: Long = 0L,
    val title: String,
    val description: String?,
    val placeIds: List<Long>        // 코스에 포함된 place 리스트
)