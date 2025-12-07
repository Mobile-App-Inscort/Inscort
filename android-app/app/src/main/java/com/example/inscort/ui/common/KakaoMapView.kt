package com.example.inscort.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import com.example.inscort.util.DeviceUtils          // 에뮬레이터 체크 유틸
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@Composable
fun KakaoMapView(
    modifier: Modifier = Modifier,
    onMapReady: (KakaoMap) -> Unit // 지도가 준비되면 불리는 콜백
) {
    if (DeviceUtils.isEmulator()) {
        // ✅ 에뮬레이터: MapView 아예 생성 안 함 (네이티브 라이브러리 로드 X)
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "에뮬레이터에서는 카카오맵을 표시하지 않아요.\n실기기에서 확인해 주세요.",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // 실기기: 기존처럼 MapView 생성
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