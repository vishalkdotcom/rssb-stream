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
import com.vishalk.rssbstream.presentation.screens.*
import com.vishalk.rssbstream.presentation.screens.rssb.*
import com.vishalk.rssbstream.presentation.screens.rssb.AudiobookDetailScreen
import com.vishalk.rssbstream.presentation.viewmodel.PlayerViewModel
import com.vishalk.rssbstream.presentation.viewmodel.PlaylistViewModel

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


        // ===== Legacy Local Music Screens (Hidden but kept for reference/compilation) =====

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
            RssbScreen.NavBarCornerRadius.route,
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
