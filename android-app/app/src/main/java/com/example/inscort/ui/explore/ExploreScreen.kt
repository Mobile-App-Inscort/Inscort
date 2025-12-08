package com.example.inscort.ui.explore

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onBack: () -> Unit,
    onNavigateToBuilder: (List<Place>) -> Unit
) {
    val context = LocalContext.current

    // ViewModel 생성
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

    // 진입 시 샘플 이미지로 OCR 수행
    LaunchedEffect(Unit) {
        val resId = R.drawable.image
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        viewModel.runOcr(listOf(inputImage to "sample_drawable"))
    }

    // OCR + Kakao Local 결과
    val uiState by viewModel.uiState.collectAsState()
    val places = uiState.placeSuggestions

    // 선택된 장소 리스트
    val selectedPlaces = remember { mutableStateListOf<Place>() }

    // 마커로 “선택(포커스)”된 장소 (사이드 카드용)
    var focusedPlace by remember { mutableStateOf<Place?>(null) }

    // 지도 컨트롤러
    var mapController by remember { mutableStateOf<KakaoMapController?>(null) }

    // 초기 한 번만 카메라 이동했는지 여부
    var initialCameraMoved by remember { mutableStateOf(false) }

    // 장소 목록이 새로 들어올 때는 선택/포커스 초기화
    LaunchedEffect(places) {
        selectedPlaces.clear()
        focusedPlace = null
        initialCameraMoved = false
    }

    // 마커 다시 그리는 로직 (선택 여부에 따라 아이콘 달리)
    LaunchedEffect(places, selectedPlaces.toList(), mapController) {
        if (places.isNotEmpty() && mapController != null) {
            mapController?.clear()
            places.forEach { place ->
                val iconResId =
                    if (selectedPlaces.contains(place)) R.drawable.ic_marker_selected
                    else R.drawable.ic_marker

                mapController?.addMarker(
                    lat = place.latitude,
                    lng = place.longitude,
                    name = place.name,
                    iconResId = iconResId
                )
            }
            // 🔹 처음 한 번만 기본 위치로 카메라 이동
            if (!initialCameraMoved) {
                mapController?.moveCamera(places[0].latitude, places[0].longitude)
                initialCameraMoved = true
            }
        }
    }

    // 마커 클릭 리스너: 항상 최신 places 기준으로 동작
    LaunchedEffect(mapController, places) {
        val controller = mapController ?: return@LaunchedEffect

        controller.setOnMarkerClickListener { markerName ->
            val clickedPlace = places.find { it.name == markerName }
            if (clickedPlace != null) {
                // 선택 토글
                if (selectedPlaces.contains(clickedPlace)) {
                    selectedPlaces.remove(clickedPlace)
                } else {
                    selectedPlaces.add(clickedPlace)
                }

                // 사이드 상세 카드용 포커스
                focusedPlace = clickedPlace

                // 카메라 이동
                controller.moveCamera(
                    lat = clickedPlace.latitude,
                    lng = clickedPlace.longitude,
                    zoomLevel = 17
                )
            }
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

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
                    // ⬆️ 바텀시트에서 클릭 시에는 “선택만” 바꾸고
                    //    focusedPlace 는 건드리지 않는다
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
            // 1. 지도
            KakaoMapView(
                modifier = Modifier.fillMaxSize(),
                placeSuggestions = places,
                onMapReady = { kakaoMap ->
                    mapController = KakaoMapController(kakaoMap)
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

            // 3. 상단 오른쪽 “N개 장소 추출됨” 칩
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

            // 4. 오른쪽 사이드 상세 카드 (마커 클릭 시에만 표시)
            if (focusedPlace != null) {
                FocusedPlaceCard(
                    focusedPlace = focusedPlace!!,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, top = 72.dp, bottom = 24.dp)
                )
            }
        }
    }
}

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
        // 상단 그립바
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 8.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray)
                .align(Alignment.CenterHorizontally)
        )

        // 제목
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

        // 장소 리스트
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

        // 하단 버튼
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

@Composable
fun FocusedPlaceCard(
    focusedPlace: Place,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .width(260.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(focusedPlace.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                focusedPlace.address ?: "주소 정보 없음",
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!focusedPlace.sourceUrl.isNullOrBlank()) {
                AsyncImage(
                    model = focusedPlace.sourceUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 카카오맵 상세 보기 (현재는 sourceUrl을 외부 URL이라고 가정)
            if (!focusedPlace.sourceUrl.isNullOrBlank()) {
                Text(
                    text = "카카오맵에서 상세 정보 보기",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(focusedPlace.sourceUrl)
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}
