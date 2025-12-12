package com.vishalk.rssbstream.presentation.navigation

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vishalk.rssbstream.data.preferences.CarouselStyle
import com.vishalk.rssbstream.data.preferences.LaunchTab
import com.vishalk.rssbstream.data.preferences.UserPreferencesRepository
import com.vishalk.rssbstream.presentation.screens.AlbumDetailScreen
import com.vishalk.rssbstream.presentation.screens.ArtistDetailScreen
import com.vishalk.rssbstream.presentation.screens.DailyMixScreen
import com.vishalk.rssbstream.presentation.screens.EditTransitionScreen
import com.vishalk.rssbstream.presentation.screens.GenreDetailScreen
import com.vishalk.rssbstream.presentation.screens.HomeScreen
import com.vishalk.rssbstream.presentation.screens.LibraryScreen
import com.vishalk.rssbstream.presentation.screens.MashupScreen
import com.vishalk.rssbstream.presentation.screens.NavBarCornerRadiusScreen
import com.vishalk.rssbstream.presentation.screens.PlaylistDetailScreen
import com.vishalk.rssbstream.presentation.screens.AboutScreen
import com.vishalk.rssbstream.presentation.screens.SearchScreen
import com.vishalk.rssbstream.presentation.screens.StatsScreen
import com.vishalk.rssbstream.presentation.screens.SettingsScreen
import com.vishalk.rssbstream.presentation.screens.rssb.AudiobooksScreen
import com.vishalk.rssbstream.presentation.screens.rssb.DiscoursesScreen
import com.vishalk.rssbstream.presentation.screens.rssb.QnaScreen
import com.vishalk.rssbstream.presentation.screens.rssb.RssbHomeScreen
import com.vishalk.rssbstream.presentation.screens.rssb.ShabadsScreen
import com.vishalk.rssbstream.presentation.viewmodel.PlayerViewModel
import com.vishalk.rssbstream.presentation.viewmodel.PlaylistViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

@OptIn(UnstableApi::class)
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun AppNavigation(
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    paddingValues: PaddingValues,
    userPreferencesRepository: UserPreferencesRepository
) {
    var launchTab by remember { mutableStateOf(LaunchTab.HOME) }

    // Collect the initial value once and never again
    LaunchedEffect(Unit) {
        userPreferencesRepository.launchTabFlow
            .first() // Get only the first value
            .let { tab ->
                launchTab = tab
            }
    }

    NavHost(
        navController = navController,
        startDestination = launchTab
    ) {
        composable(
            Screen.Home.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            HomeScreen(navController = navController, paddingValuesParent = paddingValues, playerViewModel = playerViewModel)
        }
        composable(
            Screen.Search.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { enterTransition() },
            popExitTransition = { exitTransition() },
        ) {
            SearchScreen(paddingValues = paddingValues, playerViewModel = playerViewModel, navController = navController)
        }
            composable(
                Screen.Library.route,
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) {
                LibraryScreen(navController = navController, playerViewModel = playerViewModel)
            }
            composable(
                Screen.Settings.route,
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
                Screen.DailyMixScreen.route,
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) {
                DailyMixScreen(
                    playerViewModel = playerViewModel,
                    navController = navController
                )
            }
            composable(
                Screen.Stats.route,
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) {
                StatsScreen(
                    navController = navController
                )
            }
            composable(
                route = Screen.PlaylistDetail.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId")
                val playlistViewModel: PlaylistViewModel = hiltViewModel()
                if (playlistId != null) {
                    PlaylistDetailScreen(
                        playlistId = playlistId,
                        playerViewModel = playerViewModel,
                        playlistViewModel = playlistViewModel,
                        onBackClick = { navController.popBackStack() },
                        onDeletePlayListClick = { navController.popBackStack() },
                        navController = navController
                    )
                }
            }
            composable(
                Screen.DJSpace.route,
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) {
                MashupScreen()
            }
            composable(
                route = Screen.GenreDetail.route,
                arguments = listOf(navArgument("genreId") { type = NavType.StringType }),
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) { backStackEntry ->
                val genreId = backStackEntry.arguments?.getString("genreId")
                if (genreId != null) {
                    GenreDetailScreen(
                        navController = navController,
                        genreId = genreId,
                        playerViewModel = playerViewModel
                    )
                } else {
                    Text("Error: Genre ID missing", modifier = Modifier)
                }
            }
            composable(
                route = Screen.AlbumDetail.route,
                arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId")
                if (albumId != null) {
                    AlbumDetailScreen(
                        albumId = albumId,
                        navController = navController,
                        playerViewModel = playerViewModel
                    )
                }
            }
            composable(
                route = Screen.ArtistDetail.route,
                arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId")
                if (artistId != null) {
                    ArtistDetailScreen(
                        artistId = artistId,
                        navController = navController,
                        playerViewModel = playerViewModel
                    )
                }
            }
            composable(
                "nav_bar_corner_radius",
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) {
                NavBarCornerRadiusScreen(navController)
            }
            composable(
                route = Screen.EditTransition.route,
                arguments = listOf(navArgument("playlistId") {
                    type = NavType.StringType
                    nullable = true
                }),
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) {
                EditTransitionScreen(navController = navController)
            }
            composable(
                Screen.About.route,
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
            
            // ===== RSSB Content Screens =====
            
            composable(
                RssbScreen.RssbHome.route,
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { enterTransition() },
                popExitTransition = { exitTransition() },
            ) {
                RssbHomeScreen(
                    navController = navController,
                    paddingValues = paddingValues
                )
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
        }
}