package com.example.inscort.data.mapper

import com.example.inscort.core.model.Place
import com.example.inscort.data.local.entity.PlaceEntity

// Entity → Domain
fun PlaceEntity.toModel(): Place {
    return Place(
        id = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        sourceUrl = sourceUrl
    )
}

// Domain → Entity
fun Place.toEntity(): PlaceEntity {
    return PlaceEntity(
        id = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        sourceUrl = sourceUrl
    )
}
