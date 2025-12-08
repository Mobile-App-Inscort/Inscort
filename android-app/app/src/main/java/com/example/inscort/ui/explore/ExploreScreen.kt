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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.inscort.R
import com.example.inscort.core.model.Place
import com.example.inscort.ui.common.KakaoMapController
import com.example.inscort.ui.common.KakaoMapView

@OptIn(ExperimentalMaterial3Api::class) // BottomSheetScaffold는 아직 실험적 기능이라 이거 필요
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = viewModel(),
    onBack: () -> Unit, //뒤로 가기 동작
    onNavigateToBuilder: (List<Place>) -> Unit // 코스 빌더로 이동
) {
    val places by viewModel.places.collectAsState()
    val selectedPlaces = viewModel.selectedPlaces
    var mapController by remember { mutableStateOf<KakaoMapController?>(null) }

    // 마커 찍기 로직 (선택된 건 색깔 다르게 하거나 할 수 있음)
    LaunchedEffect(places, mapController) {
        if (places.isNotEmpty() && mapController != null) {
            // 1. 기존 마커 지우기
            mapController?.clear()

            // 2. 마커 추가
            val coords = places.map { Pair(it.latitude, it.longitude) }
            mapController?.addMarkers(coords, R.drawable.ic_marker)

            // 3. 카메라 이동 (첫 번째 장소로)
            mapController?.moveCamera(places[0].latitude, places[0].longitude)

            // 로그로 확인
            android.util.Log.d("MapDebug", "마커 찍기 성공! 지도 준비됨.")
        } else {
            android.util.Log.d("MapDebug", "대기 중... (데이터: ${places.size}개, 지도: ${if(mapController==null) "X" else "O"})")
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            BottomSheetContent(
                places = places,
                selectedPlaces = selectedPlaces,
                onToggle = { viewModel.toggleSelection(it) },
                onConfirm = {
                    // ★ 선택된 장소들을 가지고 다음 화면(빌더)으로 이동!
                    onNavigateToBuilder(selectedPlaces.toList())
                }
            )
        },
        sheetPeekHeight = 220.dp, // 살짝 올라와 있는 높이
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
                onMapReady = { kakaoMap ->
                    val controller = KakaoMapController(kakaoMap)
                    mapController = controller

                    // ▼▼▼ [추가] 마커 클릭 리스너 등록! ▼▼▼
                    controller.setOnMarkerClickListener { markerName ->
                        // 1. 이름으로 장소 찾기
                        val clickedPlace = places.find { it.name == markerName }

                        // 2. 찾았으면 선택(Toggle) 하기
                        if (clickedPlace != null) {
                            viewModel.toggleSelection(clickedPlace)
                        }
                    }
                }
            )

            // 2. [상단 왼쪽] 뒤로 가기 버튼
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp) // 상태바 높이 고려
                    .size(48.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // 화살표 아이콘
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            // 3. [상단 오른쪽] "N개 장소 추출됨" 칩
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
    val isReady = selectedPlaces.isNotEmpty() // 하나라도 선택했는지?

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp) // 최대로 늘어날 높이
    ) {
        // 핸들 바
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 8.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray)
                .align(Alignment.CenterHorizontally)
        )

        // 타이틀
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

        // 리스트 (장소 목록)
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

        // ★ [하단 버튼] 상태에 따라 바뀜
        Button(
            onClick = onConfirm,
            enabled = isReady, // 선택 안 하면 비활성화
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isReady) Color(0xFFFF8A80) else Color(0xFFE0E0E0), // 활성: 분홍, 비활성: 회색
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
// 개별 리스트 아이템 UI (사진 + 텍스트 + 동그라미 선택버튼)
@Composable
fun PlaceListItem(
    place: Place,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 선택 여부에 따른 색상 정의
    val borderColor = if (isSelected) Color(0xFFFF8A80) else Color(0xFFEEEEEE) // 분홍 vs 연회색
    val checkColor = if (isSelected) Color(0xFFFF8A80) else Color(0xFFE0E0E0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            // ★ 테두리 및 배경 적용
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp), // 내부 패딩
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 썸네일 이미지
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

        // 텍스트 정보
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

        // ★ 체크박스 아이콘 (동그라미)
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
