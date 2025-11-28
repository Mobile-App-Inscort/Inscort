// data/local/dao/PlaceDao.kt
package com.example.inscort.data.local.dao

import androidx.room.*
import com.example.inscort.data.local.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: PlaceEntity): Long

    @Query("SELECT * FROM places")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Delete
    suspend fun delete(place: PlaceEntity)
}
