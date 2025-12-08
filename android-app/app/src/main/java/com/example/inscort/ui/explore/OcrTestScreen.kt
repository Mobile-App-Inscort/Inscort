package com.example.inscort.ui.explore


import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.inscort.ui.explore.CourseDiscoveryViewModel
import com.example.inscort.R
import com.google.mlkit.vision.common.InputImage

@Composable
fun OcrTestScreen(
    viewModel: CourseDiscoveryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(text = "ML Kit OCR 테스트")

        Button(
            onClick = {
                // 1) drawable 에 넣어둔 샘플 이미지 로드
                val resId = R.drawable.image // <- 더미데이터 이미지 이름
                val bitmap = BitmapFactory.decodeResource(context.resources, resId)
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                // 2) ViewModel 통해 OCR 실행
                viewModel.runOcr(
                    listOf(inputImage to "sample_drawable")
                )
            }
        ) {
            Text("샘플 이미지로 OCR 실행")
        }

        // 로딩 표시
        if (uiState.loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("인식 중...")
            }
        }

        // 에러 메시지
        uiState.errorMessage?.let { msg ->
            Text(text = "에러: $msg")
        }

// OCR 텍스트 결과
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.results) { result ->
                Text("이미지: ${result.imageUrl}")
                result.lines.forEach { line ->
                    Text("- $line")
                }
            }
        }

// Kakao 검색 결과
        Text("===== Kakao 장소 후보 =====")

        uiState.placeSuggestions.forEach { place ->
            Text("· ${place.name} (${place.address ?: "주소 없음"})")
        }

    }
}
