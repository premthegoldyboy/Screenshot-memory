package com.example.screenshotmemory.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.screenshotmemory.ui.screens.home.HomeScreen
import com.example.screenshotmemory.ui.screens.home.HomeViewModel
import com.example.screenshotmemory.ui.screens.settings.SettingsScreen
import com.example.screenshotmemory.ui.screens.viewer.ScreenshotViewerScreen
import com.example.screenshotmemory.ui.screens.viewer.ScreenshotViewerViewModel

object NavRoutes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val VIEWER = "viewer/{screenshotId}"

    fun createViewerRoute(id: Long) = "viewer/$id"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    darkTheme: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val homeViewModel: HomeViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToViewer = { id ->
                    navController.navigate(NavRoutes.createViewerRoute(id))
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )
        }

        composable(
            route = NavRoutes.VIEWER,
            arguments = listOf(
                navArgument("screenshotId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("screenshotId") ?: 0L
            val viewerViewModel: ScreenshotViewerViewModel = viewModel(
                key = "viewer_$id",
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ScreenshotViewerViewModel(application, id) as T
                    }
                }
            )

            ScreenshotViewerScreen(
                viewModel = viewerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = homeViewModel,
                darkTheme = darkTheme,
                onThemeChange = onThemeChange,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
