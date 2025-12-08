package com.example.inscort

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.inscort.core.model.Place
import com.example.inscort.ui.builder.CourseBuilderScreen
import com.example.inscort.ui.detail.CourseDetailScreen
import com.example.inscort.ui.explore.ExploreScreen // [중요] 우리가 만든 화면 Import
import com.example.inscort.core.ocr.OcrService
import com.example.inscort.data.repository.MlKitOcrService
import com.example.inscort.data.repository.PlaceRepository
import com.example.inscort.ui.explore.CourseDiscoveryViewModel
import com.example.inscort.ui.explore.OcrTestScreen
import com.example.inscort.ui.theme.InscortTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱이 켜지면 여기서부터 화면을 그립니다.
        setContent {
            MaterialTheme {
                Surface {
                    // 1. 네비게이션 컨트롤러 생성 (길 안내자)
                    val navController = rememberNavController()

                    // 2. 화면 간 데이터 전달을 위한 임시 저장소
                    // (Explore에서 선택한 장소들을 여기에 담아서 Builder로 넘겨줌)
                    var selectedPlacesForBuilder by remember { mutableStateOf<List<Place>>(emptyList()) }

                    // 3. 네비게이션 호스트 (화면 갈아끼우는 틀)
                    NavHost(
                        navController = navController,
                        startDestination = "explore" // 앱 켜지면 '탐색' 부터 시작
                    ) {

                        // [화면 1] 탐색 화면 (Explore)
                        composable("explore") {
                            ExploreScreen(
                                onBack = { finish() }, // 첫 화면에서 뒤로가기면 앱 종료
                                onNavigateToBuilder = { places ->
                                    // (1) 선택한 장소 리스트를 임시 저장소에 담음
                                    selectedPlacesForBuilder = places
                                    // (2) 빌더 화면으로 이동!
                                    navController.navigate("builder")
                                }
                            )
                        }

                        // [화면 2] 코스 빌더 (Builder)
                        composable("builder") {
                            CourseBuilderScreen(
                                // (3) 아까 담아둔 장소들을 꺼내서 전달
                                selectedPlaces = selectedPlacesForBuilder,
                                onBack = {
                                    navController.popBackStack() // 뒤로가기 (탐색 화면으로)
                                },
                                onSaveComplete = { newCourseId ->
                                    // (4) 저장 완료되면 상세 화면으로 이동 (ID 들고 감)
                                    navController.navigate("detail/$newCourseId") {
                                        // (선택) 빌더 화면은 백스택에서 지워버리기 (뒤로가기하면 탐색으로 가게)
                                        popUpTo("explore")
                                    }
                                }
                            )
                        }

                        // [화면 3] 코스 상세 (Detail)
                        composable("detail/{courseId}") { backStackEntry ->
                            // URL에서 courseId 꺼내기
                            val courseId = backStackEntry.arguments?.getString("courseId")?.toLongOrNull() ?: 0L

                            CourseDetailScreen(
                                courseId = courseId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
