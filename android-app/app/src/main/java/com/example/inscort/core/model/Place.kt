package com.example.inscort.core.model

data class Place(
    val id: Long = 0L,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val sourceUrl: String? = null   // 인스타 링크 등
)