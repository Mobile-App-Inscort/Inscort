package com.example.inscort.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InstagramLinkScreen(
    viewModel: CourseDiscoveryViewModel,
    onBackClick: () -> Unit = {},
    onNavigateToExplore: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var urlText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.step, uiState.loading) {
        if (uiState.step == AnalysisStep.Done && !uiState.loading) {
            onNavigateToExplore()
        }
    }

    // 에러 메시지 스낵바
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                    Text(
                        text = "인스타 링크 분석",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        // 🔥 loading 여부에 따라 화면 갈아끼우기
        if (uiState.loading) {
            AnalysisProgressScreen(
                progress = uiState.progress,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        } else {
            InstagramLinkInputScreen(
                urlText = urlText,
                onUrlChange = { urlText = it },
                isLoading = uiState.loading,
                onAnalyzeClick = {
                    if (urlText.isNotBlank()) {
                        viewModel.crawlAndRunOcr(urlText.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

/**
 * 🔹 평소에 보이는 "링크 입력" 화면
 */
@Composable
private fun InstagramLinkInputScreen(
    urlText: String,
    onUrlChange: (String) -> Unit,
    isLoading: Boolean,
    onAnalyzeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // 상단 인스타 카드
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFFF1F3)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, shape = CircleShape)
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color(0xFFFF4F7A)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "링크 하나로 자동 코스 생성",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "맛집·카페 모음 게시물의 링크를 입력하면\n장소를 자동으로 추출해드릴게요.",
                            fontSize = 13.sp,
                            color = Color(0xFF777777)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "인스타그램 게시물 링크" 라벨
            Text(
                text = "인스타그램 게시물 링크",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 링크 입력 필드
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = urlText,
                onValueChange = onUrlChange,
                singleLine = true,
                placeholder = {
                    Text("https://www.instagram.com/p/...")
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = Color(0xFFBBBBBB)
                    )
                }
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "여러 장소가 포함된 게시물 링크를 붙여주세요.",
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 예시 게시물 카드
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF7F9FF)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF5B7CFF)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "예시 게시물",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• \"강남 핫플 카페 5곳 모음 ☕\"\n" +
                                "• \"홍대 데이트 코스 완벽 정리 💜\"\n" +
                                "• \"상수동 맛집 투어 추천 1탄\"",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 링크 분석하기 버튼
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = onAnalyzeClick,
                enabled = urlText.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7A94),
                    disabledContainerColor = Color(0xFFFFC5D0)
                )
            ) {
                Text(
                    text = "링크 분석하기",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 분석 과정 설명
            Text(
                text = "분석 과정",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF444444)
            )
            Spacer(modifier = Modifier.height(12.dp))

            AnalysisStepRow(
                number = "1",
                title = "게시물 이미지 수집",
                desc = "Instagram 크롤링으로 게시물 이미지를 가져와요."
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnalysisStepRow(
                number = "2",
                title = "텍스트 인식 (OCR)",
                desc = "이미지에서 가게 이름, 위치 등을 추출해요."
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnalysisStepRow(
                number = "3",
                title = "지도 좌표 변환",
                desc = "Kakao Map API로 실제 좌표를 찾아 코스를 만들어 드려요."
            )
        }
    }
}

/**
 * 🔹 크롤링/분석이 진행되는 동안 보여줄 프로그레스 화면
 */
@Composable
private fun AnalysisProgressScreen(
    progress: Int,
    modifier: Modifier = Modifier
) {
    val progressFraction = (progress.coerceIn(0, 100)) / 100f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 큰 원 + 인디케이터
        Box(
            modifier = Modifier
                .height(180.dp)
                .width(180.dp)
                .background(color = Color(0xFFFFF1F3), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = progressFraction,
                strokeWidth = 8.dp,
                color = Color(0xFFFF7A94),
                modifier = Modifier
                    .height(120.dp)
                    .width(120.dp)
            )
        }

        Text(
            text = "장소 정보를 분석하고 있어요...",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "잠시만 기다려주세요",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )

        // 퍼센트 바
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            LinearProgressIndicator(
                progress = progressFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${progress.coerceIn(0, 100)}%",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 단계 설명 (고정 텍스트)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF9FAFB)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "분석 과정",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF444444)
                )

                AnalysisStepRow(
                    number = "1",
                    title = "이미지 텍스트 인식",
                    desc = "게시물 속 이미지에서 텍스트를 추출하고 있어요."
                )
                AnalysisStepRow(
                    number = "2",
                    title = "장소 정보 파싱",
                    desc = "추출된 텍스트에서 가게 이름과 위치를 분석 중이에요."
                )
                AnalysisStepRow(
                    number = "3",
                    title = "지도 좌표로 변환",
                    desc = "Kakao Map API로 실제 좌표를 찾는 중이에요."
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AnalysisStepRow(
    number: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFFFE4EA), CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF4F7A)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}
