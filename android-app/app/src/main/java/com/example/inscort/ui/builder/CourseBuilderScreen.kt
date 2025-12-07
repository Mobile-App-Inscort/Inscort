package com.example.inscort.ui.builder

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.example.inscort.data.local.db.AppDatabase
import com.example.inscort.data.repository.PlaceRepository
// ▼▼▼ [변경] 새로운 라이브러리 import ▼▼▼
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.example.inscort.data.api.KakaoRetrofitProvider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseBuilderScreen(
    selectedPlaces: List<Place>,
    onBack: () -> Unit,
    onSaveComplete: (Long) -> Unit
) {
    val context = LocalContext.current
    val repository = remember {
        val db = AppDatabase.getInstance(context)
        PlaceRepository(
            db.placeDao(),
            KakaoNaviApi.create(),
            localApi = KakaoRetrofitProvider.localApi)
    }

    val viewModel: CourseBuilderViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CourseBuilderViewModel(repository) as T
            }
        }
    )

    // ▼▼▼ [수정 1] 리스트 상태를 직접 만듭니다. ▼▼▼
    val listState = rememberLazyListState()

    // ▼▼▼ [수정 2] 만든 listState를 여기에 넣어줍니다. ▼▼▼
    val state = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, to ->
            viewModel.moveItem(from.index, to.index)
        }
    )

    LaunchedEffect(Unit) {
        if (viewModel.places.isEmpty()) {
            viewModel.initPlaces(selectedPlaces)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // [1] 상단바
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("코스 빌더", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // [2] 내용
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
        ) {
            Text("코스 이름", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.courseTitle,
                onValueChange = { viewModel.courseTitle = it },
                placeholder = { Text("강남 데이트 코스") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("장소 순서 설정", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // [변경] 드래그 리스트 구현
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(viewModel.places, key = { _, place -> place.id }) { index, place ->

                    // [변경] ReorderableItem 사용법 변경
                    ReorderableItem(state, key = place.id) { isDragging ->
                        val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation")

                        DraggablePlaceItem(
                            index = index + 1,
                            place = place,
                            // ★ 여기가 핵심: 핸들에 드래그 기능 부여
                            handleModifier = Modifier.draggableHandle(),
                            modifier = Modifier.shadow(elevation.value)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    AddPlaceButton {}
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // [3] 하단 버튼
        Button(
            onClick = {
                viewModel.saveCourse { courseId -> onSaveComplete(courseId) }
            },
            enabled = viewModel.isSaveEnabled,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF8A80),
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("코스 저장하기", color = if (viewModel.isSaveEnabled) Color.White else Color.Gray)
        }
    }
}

// [변경] handleModifier 파라미터 추가 (아이콘에 드래그 기능 넣기 위해)
@Composable
fun DraggablePlaceItem(
    index: Int,
    place: Place,
    handleModifier: Modifier = Modifier, // 드래그 핸들용 수정자
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // [변경] 아이콘에 handleModifier 적용 -> 이제 이걸 잡고 끌면 움직임!
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "move",
                    tint = Color.LightGray,
                    modifier = handleModifier
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier.size(24.dp).background(Color(0xFFFF8A80), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$index", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                AsyncImage(
                    model = place.sourceUrl ?: "",
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(place.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(place.address ?: "", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
            }
            // (메모 박스 생략 - 위와 동일)
        }
    }
}

@Composable
fun AddPlaceButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
            Text(" 장소 추가하기", color = Color.Gray)
        }
    }
}