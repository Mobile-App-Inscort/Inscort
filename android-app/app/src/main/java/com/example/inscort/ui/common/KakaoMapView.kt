// KakaoMapView.kt
package com.example.inscort.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@Composable
fun KakaoMapView(
    modifier: Modifier = Modifier,
    onMapReady: (KakaoMap) -> Unit // 지도가 준비되면 불리는 콜백
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                start(object : MapLifeCycleCallback() {
                    override fun onMapDestroy() {
                        // 지도 파괴 시 처리
                    }

                    override fun onMapError(error: Exception?) {
                        android.util.Log.e("KakaoMap", "지도 에러 발생: ${error?.message}")
                        // 에러 처리
                    }
                }, object : KakaoMapReadyCallback() {
                    override fun onMapReady(kakaoMap: KakaoMap) {
                        // 지도가 진짜 로딩 끝났을 때!
                        onMapReady(kakaoMap)
                    }
                })
            }
        }
    )
}