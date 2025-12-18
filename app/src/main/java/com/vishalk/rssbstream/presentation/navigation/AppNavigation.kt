package com.vishalk.rssbstream.presentation.navigation

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vishalk.rssbstream.data.preferences.UserPreferencesRepository
import com.vishalk.rssbstream.presentation.screens.AboutScreen
import com.vishalk.rssbstream.presentation.screens.SettingsScreen
import com.vishalk.rssbstream.presentation.screens.rssb.*
import com.vishalk.rssbstream.presentation.screens.rssb.AudiobookDetailScreen
import com.vishalk.rssbstream.presentation.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@SuppressLint("UnrememberedGetBackStackEntry", "NewApi")
@Composable
fun AppNavigation(
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    paddingValues: PaddingValues,
    userPreferencesRepository: UserPreferencesRepository
) {
    NavHost(
        navController = navController,
        startDestination = RssbScreen.RssbHome.route
    ) {
        // ===== RSSB Content Screens (Primary) =====

        composable(
            RssbScreen.RssbHome.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            RssbHomeScreen(
                navController = navController,
                paddingValues = paddingValues,
                playerViewModel = playerViewModel
            )
        }

        composable(
            RssbScreen.Search.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            RssbSearchScreen(navController = navController)
        }

        composable(
            RssbScreen.Library.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            RssbLibraryScreen(navController = navController)
        }

        composable(
            RssbScreen.Audiobooks.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            AudiobooksScreen(navController = navController)
        }

        composable(
            RssbScreen.QnA.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            QnaScreen(navController = navController)
        }

        composable(
            RssbScreen.Shabads.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            ShabadsScreen(navController = navController)
        }

        composable(
            RssbScreen.Discourses.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            DiscoursesScreen(navController = navController)
        }

        composable(
            route = RssbScreen.AudiobookDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            if (id != null) {
                AudiobookDetailScreen(
                    audiobookId = id,
                    navController = navController,
                    playerViewModel = playerViewModel
                )
            }
        }


        composable(
            RssbScreen.Settings.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            SettingsScreen(
                navController = navController,
                playerViewModel = playerViewModel,
                onNavigationIconClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            RssbScreen.About.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            AboutScreen(
                navController = navController,
                onNavigationIconClick = { navController.popBackStack() }
            )
        }
    }
}
