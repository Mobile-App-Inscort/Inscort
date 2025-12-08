package com.example.inscort.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.inscort.R
import com.example.inscort.core.model.Place
import com.example.inscort.core.ocr.OcrService
import com.example.inscort.data.api.KakaoNaviApi
import com.example.inscort.data.api.KakaoRetrofitProvider
import com.example.inscort.data.local.DatabaseProvider
import com.example.inscort.data.repository.MlKitOcrService
import com.example.inscort.data.repository.PlaceRepository
import com.example.inscort.ui.common.KakaoMapController
import com.example.inscort.ui.common.KakaoMapView
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onBack: () -> Unit,
    onNavigateToBuilder: (List<Place>) -> Unit
) {
    val context = LocalContext.current

    // ✅ 옵션 A: ExploreViewModel → CourseDiscoveryViewModel 로 교체
    val viewModel: CourseDiscoveryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = DatabaseProvider.get(context)
                val placeDao = db.placeDao()
                val ocrService: OcrService = MlKitOcrService()
                val naviApi = KakaoNaviApi.create()
                val placeRepository = PlaceRepository(
                    placeDao = placeDao,
                    naviApi = naviApi,
                    localApi = KakaoRetrofitProvider.localApi,
                    kakaoApiKey = com.example.inscort.BuildConfig.KAKAO_REST_API_KEY
                )
                return CourseDiscoveryViewModel(
                    ocrService = ocrService,
                    placeRepository = placeRepository
                ) as T
            }
        }
    )

    // 🔥 화면 진입 시, drawable 샘플 이미지로 OCR 한 번 실행
    LaunchedEffect(Unit) {
        val resId = R.drawable.image   // OcrTestScreen에서 쓰던 샘플 이미지
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        viewModel.runOcr(
            listOf(inputImage to "sample_drawable")
        )
    }

    // 🔁 OCR + Kakao Local 결과 상태
    val uiState by viewModel.uiState.collectAsState()
    val places = uiState.placeSuggestions

    // ✅ 선택된 장소는 화면 쪽에서 로컬 state 로 관리
    val selectedPlaces = remember { mutableStateListOf<Place>() }

    // 새로운 장소 목록이 들어오면 선택 상태 초기화
    LaunchedEffect(places) {
        selectedPlaces.clear()
    }

    var mapController by remember { mutableStateOf<KakaoMapController?>(null) }

    // 마커 찍기 로직
    LaunchedEffect(places, mapController) {
        if (places.isNotEmpty() && mapController != null) {
            mapController?.clear()

            val coords = places.map { it.latitude to it.longitude }
            mapController?.addMarkers(coords, R.drawable.ic_marker)

            mapController?.moveCamera(places[0].latitude, places[0].longitude)

            android.util.Log.d("MapDebug", "마커 찍기 성공! 지도 준비됨.")
        } else {
            android.util.Log.d(
                "MapDebug",
                "대기 중... (데이터: ${places.size}개, 지도: ${if (mapController == null) "X" else "O"})"
            )
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            BottomSheetContent(
                places = places,
                selectedPlaces = selectedPlaces,
                onToggle = { place ->
                    if (selectedPlaces.contains(place)) {
                        selectedPlaces.remove(place)
                    } else {
                        selectedPlaces.add(place)
                    }
                },
                onConfirm = {
                    onNavigateToBuilder(selectedPlaces.toList())
                }
            )
        },
        sheetPeekHeight = 220.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = Color.White,
        sheetShadowElevation = 10.dp
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. 지도 (배경)
            KakaoMapView(
                modifier = Modifier.fillMaxSize(),
                placeSuggestions = places, // 에뮬레이터용 데이터 전달
                onMapReady = { kakaoMap ->
                    val controller = KakaoMapController(kakaoMap)
                    mapController = controller

                    // ▼▼▼ [HEAD와 Incoming 기능 병합] ▼▼▼
                    // 1. 마커 클릭 리스너 등록 (HEAD 기능)
                    controller.setOnMarkerClickListener { markerName ->
                        // 이름으로 장소 찾아서 선택 상태 토글
                        val clickedPlace = places.find { it.name == markerName }
                        if (clickedPlace != null) {
                            if (selectedPlaces.contains(clickedPlace)) {
                                selectedPlaces.remove(clickedPlace)
                            } else {
                                selectedPlaces.add(clickedPlace)
                            }
                        }
                    }
                }
            )

            // 2. 상단 왼쪽 뒤로가기 버튼
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp)
                    .size(48.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            // 3. 상단 오른쪽 "N개 장소 추출됨" 칩
            if (places.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .padding(top = 48.dp, end = 16.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "${places.size}개 장소 추출됨",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ▼▼▼ 바텀시트 내부 UI (리스트 + 버튼) ▼▼▼
@Composable
fun BottomSheetContent(
    places: List<Place>,
    selectedPlaces: List<Place>,
    onToggle: (Place) -> Unit,
    onConfirm: () -> Unit
) {
    val isReady = selectedPlaces.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 8.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray)
                .align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("추출된 장소 후보", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("원하는 장소를 선택해주세요", fontSize = 13.sp, color = Color.Gray)
            }
        }

        Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(places) { place ->
                PlaceListItem(
                    place = place,
                    isSelected = selectedPlaces.contains(place),
                    onClick = { onToggle(place) }
                )
            }
        }

        Button(
            onClick = onConfirm,
            enabled = isReady,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isReady) Color(0xFFFF8A80) else Color(0xFFE0E0E0),
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isReady) "${selectedPlaces.size}개 장소로 코스 만들기" else "장소를 선택해주세요",
                color = if (isReady) Color.White else Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlaceListItem(
    place: Place,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFF8A80) else Color(0xFFEEEEEE)
    val checkColor = if (isSelected) Color(0xFFFF8A80) else Color(0xFFE0E0E0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = place.sourceUrl ?: "",
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(place.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                place.address ?: "주소 정보 없음",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(28.dp)
                .background(checkColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
