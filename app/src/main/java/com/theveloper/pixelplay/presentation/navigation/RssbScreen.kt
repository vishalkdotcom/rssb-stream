package com.theveloper.pixelplay.presentation.navigation

import androidx.compose.runtime.Immutable

/**
 * Navigation routes for RSSB content screens.
 */
@Immutable
sealed class RssbScreen(val route: String) {
    object RssbHome : RssbScreen("rssb_home")
    
    object Audiobooks : RssbScreen("audiobooks")
    
    object AudiobookDetail : RssbScreen("audiobook_detail/{audiobookId}") {
        fun createRoute(audiobookId: String) = "audiobook_detail/$audiobookId"
    }
    
    object QnA : RssbScreen("qna")
    
    object Shabads : RssbScreen("shabads")
    
    object Discourses : RssbScreen("discourses")
    
    object DiscoursesByLanguage : RssbScreen("discourses/{language}") {
        fun createRoute(language: String) = "discourses/$language"
    }
    
    object ContentPlayer : RssbScreen("content_player/{contentId}") {
        fun createRoute(contentId: String) = "content_player/$contentId"
    }
    
    object Downloads : RssbScreen("downloads")
    
    object Favorites : RssbScreen("favorites")
}
