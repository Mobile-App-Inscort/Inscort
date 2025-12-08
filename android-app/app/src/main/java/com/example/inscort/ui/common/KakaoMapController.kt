package com.example.inscort.ui.common

import android.util.Log
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.route.RouteLineStyles
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer

class KakaoMapController(private val kakaoMap: KakaoMap) {

    /**
     * 1. 지도 중심 이동
     */
    fun moveCamera(lat: Double, lng: Double, zoomLevel: Int = 15) {
        val cameraUpdate = CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng), zoomLevel)
        kakaoMap.moveCamera(cameraUpdate)
    }

    /**
     * 2. 마커(Label) 하나 추가 (수정됨)
     */
    fun addMarker(lat: Double, lng: Double, name: String, iconResId: Int) {
        // [체크 1] LabelManager 가져오기
        val labelManager = kakaoMap.labelManager
        if (labelManager == null) {
            Log.e("KakaoMap", "LabelManager가 없습니다!")
            return
        }

        // [체크 2] Layer 가져오기 (없으면 기본 레이어 사용)
        val layer = labelManager.layer
        if (layer == null) {
            Log.e("KakaoMap", "Layer를 가져올 수 없습니다!")
            return
        }

        // [수정] 텍스트 스타일: 크기 15, 색상 검정색(0xFF000000)으로 설정 (흰색이면 안 보임)
        val style = LabelStyle.from(iconResId)
            .setTextStyles(15, 0xFF000000.toInt())

        // 옵션 생성
        val options = LabelOptions.from(LatLng.from(lat, lng))
            .setStyles(LabelStyles.from(style))
            .setTexts(name)
            .setTag(name)

        // [핵심] 지도에 추가하고 로그 찍기
        val label = layer.addLabel(options)

        if (label != null) {
            Log.d("KakaoMap", "📍 마커 추가 성공: $name ($lat, $lng)")
        } else {
            Log.e("KakaoMap", "❌ 마커 추가 실패 (Label이 null임)")
        }
    }

    /**
     * 3. 마커 여러 개 추가 (탐색 화면용)
     */
    fun addMarkers(places: List<Pair<Double, Double>>, iconResId: Int) {
        places.forEachIndexed { index, (lat, lng) ->
            addMarker(lat, lng, "장소 ${index + 1}", iconResId)
        }
    }

    /**
     * 4. 경로 그리기
     */
    fun drawRoute(points: List<Pair<Double, Double>>) {
        val routeLineManager = kakaoMap.routeLineManager ?: return
        val layer = routeLineManager.layer ?: return

        val style = RouteLineStyle.from(16f, android.graphics.Color.BLUE)

        val latLngs = points.map { LatLng.from(it.first, it.second) }
        val segment = RouteLineSegment.from(latLngs, RouteLineStyles.from(style))
        val options = RouteLineOptions.from(segment)

        layer.addRouteLine(options)
    }

    /**
     * 5. 초기화
     */
    fun clear() {
        kakaoMap.labelManager?.layer?.removeAll()
        kakaoMap.routeLineManager?.layer?.removeAll()
    }

    /**
     * ★ [추가] 마커 클릭 이벤트 연결하기
     * @param onClick: 마커가 클릭됐을 때 실행할 함수 (마커 이름을 돌려줌)
     */
    fun setOnMarkerClickListener(onClick: (String) -> Unit) {
        kakaoMap.setOnLabelClickListener(object : KakaoMap.OnLabelClickListener {
            override fun onLabelClicked(kakaoMap: KakaoMap, layer: LabelLayer, label: Label) {
                // 1. 마커에 심어둔 이름(Tag) 가져오기
                val markerName = label.tag?.toString() ?: ""

                // 2. UI(화면)에 클릭된 이름 전달
                onClick(markerName)

                // 3. 로그 확인
                Log.d("KakaoMap", "👆 마커 클릭됨: $markerName")

                // ★ [중요] return true; 를 지웠습니다! (이제 Void 타입이라 리턴 안 함)
            }
        })
    }
}