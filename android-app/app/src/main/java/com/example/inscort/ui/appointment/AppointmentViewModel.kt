package com.example.inscort.ui.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inscort.core.model.Appointment
import com.example.inscort.data.repository.AppointmentRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppointmentViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    val appointments: StateFlow<List<Appointment>> = repository.getAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createAppointment(
        courseId: Long,
        courseTitle: String,
        meetingName: String,
        placeCount: Int,
        date: LocalDate,
        time: LocalTime,
        onComplete: () -> Unit
    ) {
        val scheduledAt = LocalDateTime.of(date, time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModelScope.launch {
            repository.insertAppointment(
                Appointment(
                    courseId = courseId,
                    courseTitle = courseTitle,
                    meetingName = meetingName.ifBlank { "약속" },
                    placeCount = placeCount,
                    scheduledAt = scheduledAt
                )
            )
            onComplete()
        }
    }
}