package com.example.inscort.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.inscort.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity): Long

    @Query("SELECT * FROM appointments ORDER BY scheduledAt ASC")
    fun getAppointments(): Flow<List<AppointmentEntity>>
}