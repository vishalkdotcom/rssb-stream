package com.vishalk.rssbstream.presentation.navigation

import androidx.compose.runtime.Immutable

@Immutable
sealed class Screen(val route: String) {
    // Legacy local screens - kept for compilation but hidden from navigation
    // object Home : Screen("home")
    // object Search : Screen("search")
    // object Library : Screen("library")
    object Settings : Screen("settings")
    // object NavBarCrRad : Screen("nav_bar_corner_radius")
    // object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
    //    fun createRoute(playlistId: String) = "playlist_detail/$playlistId"
    // }
    // object DailyMixScreen : Screen("daily_mix")
    // object Stats : Screen("stats")
    // object GenreDetail : Screen("genre_detail/{genreId}") {
    //    fun createRoute(genreId: String) = "genre_detail/$genreId"
    // }
    // object DJSpace : Screen("dj_space")
    // object AlbumDetail : Screen("album_detail/{albumId}") {
    //    fun createRoute(albumId: Long) = "album_detail/$albumId"
    // }
    // object ArtistDetail : Screen("artist_detail/{artistId}") {
    //    fun createRoute(artistId: Long) = "artist_detail/$artistId"
    // }
    // object EditTransition : Screen("edit_transition?playlistId={playlistId}") {
    //    fun createRoute(playlistId: String?) =
    //        if (playlistId != null) "edit_transition?playlistId=$playlistId" else "edit_transition"
    // }
    object About : Screen("about")
}
