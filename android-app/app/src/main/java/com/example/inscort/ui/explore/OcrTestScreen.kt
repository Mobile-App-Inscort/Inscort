package com.example.inscort.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OcrTestScreen(
    viewModel: CourseDiscoveryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(text = "OCR 결과 미리보기")

        // 로딩 표시
        if (uiState.loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(8.dp))
                Text("인식 중...")
            }
        }

        // 에러 메시지
        uiState.errorMessage?.let { msg ->
            Text(text = "에러: $msg")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // OCR 텍스트 결과
        Text("===== OCR 텍스트 결과 =====")

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(uiState.results) { result ->
                Text("이미지: ${result.imageUrl}")
                Text(result.fullText)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Kakao 검색 결과
        Text("===== Kakao 장소 후보 =====")

        uiState.placeSuggestions.forEach { place ->
            Text("· ${place.name} (${place.address ?: "주소 없음"})")
        }
    }
}
