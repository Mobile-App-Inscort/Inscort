package com.example.inscort.ui.detail

import com.example.inscort.ui.detail.CourseDetailViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
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
import com.example.inscort.core.model.Place
import com.example.inscort.data.api.KakaoNaviApi
import com.example.inscort.data.api.KakaoRetrofitProvider
import com.example.inscort.data.local.db.AppDatabase
import com.example.inscort.data.repository.PlaceRepository
import com.example.inscort.ui.common.KakaoMapController
import com.example.inscort.ui.common.KakaoMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: Long,
    onBack: () -> Unit,
    showCreateAppointment: Boolean = true,
    onCreateAppointment: (Long, String, Int) -> Unit
) {
    // Repository & ViewModel 생성
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

    // 데이터 로드
    LaunchedEffect(courseId) {
        Log.d("CourseDetailScreen", "loadCourse courseId=$courseId")
        viewModel.loadCourse(courseId)
    }

    val places by viewModel.places.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val courseTitle by viewModel.courseTitle.collectAsState()

    var mapController by remember { mutableStateOf<KakaoMapController?>(null) }

    // 지도 그리기 (마커 + 경로)
    LaunchedEffect(places, routePoints, mapController) {
        Log.d(
            "CourseDetailScreen",
            "draw map: places=${places.size}, routePoints=${routePoints.size}, controller=${mapController != null}"
        )
        if (places.isNotEmpty() && mapController != null) {
            mapController?.clear()

            // 1. 마커 찍기 (순서 번호 포함)
            // TODO: 마커 아이콘을 순서별로 다르게 하거나(1,2,3..) 텍스트 추가 필요
            val coords = places.map { Pair(it.latitude, it.longitude) }
            Log.d("CourseDetailScreen", "addMarkers coords=$coords")
            mapController?.addMarkers(coords, com.example.inscort.R.drawable.ic_marker)

            // 2. 경로 그리기 (데이터가 왔을 때만)
            if (routePoints.isNotEmpty()) {
                Log.d("CourseDetailScreen", "drawRoute points=${routePoints.size}")
                mapController?.drawRoute(routePoints)
            }

            // 3. 카메라 이동 (전체가 보이게)
            mapController?.moveCamera(places[0].latitude, places[0].longitude)
        }
    }

    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            skipHiddenState = true,
            skipPartiallyExpanded = false,
            initialValue = SheetValue.PartiallyExpanded,
            positionalThreshold = { 0.5f },
            velocityThreshold = { 125f }
        )
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetPeekHeight = 220.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = Color(0xFFFAFAFA),
        sheetContent = {
            Column(modifier = Modifier.padding(20.dp)) {

                // 핸들 바
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 제목 & 요약
                Text(courseTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_myplaces), // 임시 아이콘
                        contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${places.size}개 장소", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("약 8.3km", color = Color.Gray, fontSize = 14.sp) // 거리 계산 로직 필요
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 버튼 (약속 잡기 / 공유하기)
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (showCreateAppointment) {
                        Button(
                            onClick = { onCreateAppointment(courseId, courseTitle, places.size) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A80)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("약속 잡기", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { /* 공유 로직 */ },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("공유하기", color = Color.Black)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { /* 공유 로직 */ },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("공유하기", color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("코스 상세", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // 장소 리스트
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp) // 하단 여백
                ) {
                    itemsIndexed(places) { index, place ->
                        CourseDetailItem(index + 1, place)

                        // 아이템 사이 연결선 (선택 사항)
                        if (index < places.lastIndex) {
                            // 점선이나 화살표 등을 그릴 수 있음
                            Box(
                                modifier = Modifier
                                    .padding(start = 32.dp) // 번호 중앙 정렬
                                    .width(2.dp)
                                    .height(20.dp)
                                    .background(Color.LightGray)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    ) { paddingValues ->
        // [1] 배경 지도
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            KakaoMapView(
                modifier = Modifier.fillMaxSize(),
                onMapReady = {
                    Log.d("CourseDetailScreen", "onMapReady")
                    mapController = KakaoMapController(it)
                }
            )

            // 뒤로가기 버튼
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp)
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
            }
        }
    }
}

@Composable
fun TipBox() {
    TODO("코스 이용 팁")
}

@Composable
fun CourseDetailItem(index: Int, place: Place) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 번호 (분홍 동그라미)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFFF8A80), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$index", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 썸네일
            AsyncImage(
                model = place.sourceUrl ?: "",
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 정보
            Column {
                Text(place.name, fontWeight = FontWeight.Bold)
                Text(place.address ?: "", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text("길찾기 >", fontSize = 12.sp, color = Color(0xFF448AFF))
            }
        }
    }
}