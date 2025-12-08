package com.example.inscort.ui.common

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.inscort.core.model.Place
import com.example.inscort.util.DeviceUtils
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@Composable
fun KakaoMapView(
    modifier: Modifier = Modifier,
    placeSuggestions: List<Place> = emptyList(), // 에뮬레이터 대체 화면용 데이터
    onMapReady: (KakaoMap) -> Unit
) {
    // 1. 에뮬레이터 체크 (수신된 코드의 기능)
    if (DeviceUtils.isEmulator()) {
        // [에뮬레이터일 경우] 지도를 띄우면 앱이 죽으니, 리스트로 대체해서 보여줌
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "에뮬레이터에서는 카카오맵을 표시하지 않아요.\n(호환성 문제 방지)",
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

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(placeSuggestions) { place ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(text = place.name, style = MaterialTheme.typography.bodyMedium)
                            Text(text = place.address ?: "주소 없음", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    } else {
        // 2. 실기기일 경우 (HEAD 코드의 수명주기 관리 로직 적용)
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val onReady by rememberUpdatedState(onMapReady)

        // MapView 인스턴스를 기억해둠 (Recomposition 방지)
        val mapView = remember {
            MapView(context).apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            Log.d("KakaoMap", "지도 파괴됨")
                        }
                        override fun onMapError(error: Exception?) {
                            Log.e("KakaoMap", "지도 에러: ${error?.message}")
                        }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            Log.d("KakaoMap", "지도 로드 완료!")
                            onReady(kakaoMap)
                        }
                    }
                )
            }
        }

        // 수명주기 연결 (이게 없으면 앱 전환 시 지도가 까맣게 변함)
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView.resume()
                    Lifecycle.Event.ON_PAUSE -> mapView.pause()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        // 지도 뷰 출력
        AndroidView(
            modifier = modifier,
            factory = { mapView }
        )
    }
}