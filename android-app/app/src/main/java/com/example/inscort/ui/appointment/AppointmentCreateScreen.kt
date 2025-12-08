package com.example.inscort.ui.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCreateScreen(
    courseId: Long,
    courseTitle: String,
    placeCount: Int,
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onAppointmentSaved: () -> Unit
) {
    var meetingName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(12, 30)) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd.") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("a hh:mm") }

    val datePicker = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    val timePicker = remember {
        android.app.TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedTime = LocalTime.of(hour, minute)
            },
            selectedTime.hour,
            selectedTime.minute,
            false
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "약속 잡기", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.createAppointment(
                            courseId = courseId,
                            courseTitle = courseTitle,
                            meetingName = meetingName,
                            placeCount = placeCount,
                            date = selectedDate,
                            time = selectedTime,
                            onComplete = onAppointmentSaved
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A80)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "약속 생성하기", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF4F3))
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = courseTitle, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${placeCount}개 장소", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = meetingName,
                    onValueChange = { meetingName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("모임명") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text("데이트") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = selectedDate.format(dateFormatter),
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePicker.show() },
                    label = { Text("날짜") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                    readOnly = true,
                    singleLine = true
                )

                OutlinedTextField(
                    value = selectedTime.format(timeFormatter),
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePicker.show() },
                    label = { Text("시간") },
                    leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                    readOnly = true,
                    singleLine = true
                )
            }
        }
    }
}