package com.example.inscort.ui.appointment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.inscort.data.local.db.AppDatabase
import com.example.inscort.data.repository.AppointmentRepository
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val CREATE_ROUTE = "appointment/create/{courseId}/{courseTitle}/{placeCount}"
private const val LIST_ROUTE = "appointments"

fun appointmentCreateRoute(courseId: Long, encodedCourseTitle: String, placeCount: Int) =
    "appointment/create/$courseId/$encodedCourseTitle/$placeCount"

fun NavGraphBuilder.appointmentNavigation(navController: NavHostController) {
    composable(
        route = CREATE_ROUTE,
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType },
            navArgument("courseTitle") { type = NavType.StringType },
            navArgument("placeCount") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val appointmentViewModel = rememberAppointmentViewModel()

        val courseIdArg = backStackEntry.arguments?.getLong("courseId") ?: 0L
        val courseTitleArg = backStackEntry.arguments?.getString("courseTitle")?.let {
            URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
        } ?: ""
        val placeCount = backStackEntry.arguments?.getInt("placeCount") ?: 0

        AppointmentCreateScreen(
            courseId = courseIdArg,
            courseTitle = courseTitleArg,
            placeCount = placeCount,
            viewModel = appointmentViewModel,
            onBack = { navController.popBackStack() },
            onAppointmentSaved = {
                navController.navigate(LIST_ROUTE) {
                    popUpTo("explore")
                }
            }
        )
    }

    composable(LIST_ROUTE) {
        val appointmentViewModel = rememberAppointmentViewModel()

        AppointmentListScreen(
            viewModel = appointmentViewModel,
            onBack = { navController.popBackStack() },
            onAppointmentSelected = { courseId ->
                navController.navigate("detail/$courseId?showCreateAppointment=false")
            }
        )
    }
}

fun encodeCourseTitle(title: String): String =
    URLEncoder.encode(title, StandardCharsets.UTF_8.toString())

@Composable
private fun rememberAppointmentViewModel(): AppointmentViewModel {
    val context = LocalContext.current
    val repository = remember { AppointmentRepository(AppDatabase.getInstance(context).appointmentDao()) }
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppointmentViewModel(repository) as T
            }
        }
    )
}