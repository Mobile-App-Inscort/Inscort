// KakaoMapView.kt
package com.example.inscort.ui.common

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
@Composable
fun KakaoMapView(
    modifier: Modifier = Modifier,
    onMapReady: (KakaoMap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onReady by rememberUpdatedState(onMapReady)

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
            // 필요 시 mapView.finish() 정책을 팀 룰에 맞게 결정
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView }
    )
}
