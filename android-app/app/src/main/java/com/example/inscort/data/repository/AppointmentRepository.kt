package com.example.inscort.data.repository

import com.example.inscort.core.model.Appointment
import com.example.inscort.data.local.dao.AppointmentDao
import com.example.inscort.data.local.entity.AppointmentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppointmentRepository(
    private val appointmentDao: AppointmentDao
) {
    suspend fun insertAppointment(appointment: Appointment): Long = withContext(Dispatchers.IO) {
        val entity = AppointmentEntity(
            id = appointment.id,
            courseId = appointment.courseId,
            courseTitle = appointment.courseTitle,
            meetingName = appointment.meetingName,
            placeCount = appointment.placeCount,
            scheduledAt = appointment.scheduledAt
        )
        appointmentDao.insert(entity)
    }

    fun getAppointments(): Flow<List<Appointment>> {
        return appointmentDao.getAppointments().map { entities ->
            entities.map { entity ->
                Appointment(
                    id = entity.id,
                    courseId = entity.courseId,
                    courseTitle = entity.courseTitle,
                    meetingName = entity.meetingName,
                    placeCount = entity.placeCount,
                    scheduledAt = entity.scheduledAt
                )
            }
        }
    }
}