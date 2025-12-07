package com.example.inscort.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.inscort.core.model.Place
import com.example.inscort.util.DeviceUtils          // 에뮬레이터 체크 유틸
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@Composable
fun KakaoMapView(
    modifier: Modifier = Modifier,
    placeSuggestions: List<Place> = emptyList(),
    onMapReady: (KakaoMap) -> Unit // 지도가 준비되면 불리는 콜백
) {
    if (DeviceUtils.isEmulator()) {
        // ✅ 에뮬레이터: MapView 아예 생성 안 하고, 안내 + 장소 리스트 보여주기
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "에뮬레이터에서는 카카오맵을 표시하지 않아요.\n실기기에서 확인해 주세요.",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            if (placeSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "인식된 장소 목록 (${placeSuggestions.size}개)",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(placeSuggestions) { place ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = place.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = place.address ?: "주소 없음",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    } else {
        // ✅ 실기기: 기존처럼 MapView 생성
        AndroidView(
            modifier = modifier,
            factory = { context ->
                MapView(context).apply {
                    start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {
                                // 지도 파괴 시 처리
                            }

                            override fun onMapError(error: Exception?) {
                                android.util.Log.e(
                                    "KakaoMap",
                                    "지도 에러 발생: ${error?.message}"
                                )
                            }
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(kakaoMap: KakaoMap) {
                                // 지도가 진짜 로딩 끝났을 때!
                                onMapReady(kakaoMap)
                            }
                        }
                    )
                }
            }
        )
    }
}
