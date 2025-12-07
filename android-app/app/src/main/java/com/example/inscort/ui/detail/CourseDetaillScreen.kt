package com.example.inscort.ui.detail

import CourseDetailViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.* // [중요] getValue, setValue가 여기 포함됨
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inscort.R
import com.example.inscort.data.api.KakaoNaviApi
import com.example.inscort.data.api.KakaoRetrofitProvider
import com.example.inscort.data.local.db.AppDatabase
import com.example.inscort.data.repository.PlaceRepository
import com.example.inscort.ui.common.KakaoMapController
import com.example.inscort.ui.common.KakaoMapView

@Composable
fun CourseDetailScreen(
    courseId: Long,
    // onBack: () -> Unit // 필요시 추가
) {
    // 1. Repository 및 ViewModel 수동 주입 (Factory 패턴)
    val context = LocalContext.current
    val repository = remember {
        val db = AppDatabase.getInstance(context)
        PlaceRepository(db.placeDao(),
            KakaoNaviApi.create(),
            localApi = KakaoRetrofitProvider.localApi)
    }

    val viewModel: CourseDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CourseDetailViewModel(repository) as T
            }
        }
    )

    // 2. 초기화 (DB에서 코스 불러오기)
    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
    }

    // 3. 상태 구독
    val places by viewModel.coursePlaces.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()

    // 4. 지도 컨트롤러 상태
    var mapController by remember { mutableStateOf<KakaoMapController?>(null) }

    // [로직 1] 장소 데이터가 로딩되면 마커 찍기
    LaunchedEffect(places) {
        // mapController가 null이 아니고, 장소가 있을 때만 실행
        if (places.isNotEmpty()) {
            mapController?.let { controller ->
                controller.clear() // 기존 것 지우기

                // 마커 찍기
                val coords = places.map { Pair(it.latitude, it.longitude) }
                // [해결] addMarkers 호출 (KakaoMapController에 이 함수가 있어야 함!)
                controller.addMarkers(coords, R.drawable.ic_marker)

                // 첫 번째 장소로 카메라 이동
                controller.moveCamera(places.first().latitude, places.first().longitude)
            }
        }
    }

    // [로직 2] 경로 데이터가 들어오면 선 긋기
    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty()) {
            mapController?.let { controller ->
                // [해결] drawRoute 호출
                controller.drawRoute(routePoints)
            }
        }
    }

    // 5. UI 그리기
    Box(modifier = Modifier.fillMaxSize()) {
        KakaoMapView(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { kakaoMap ->
                mapController = KakaoMapController(kakaoMap)
            }
        )
    }
}