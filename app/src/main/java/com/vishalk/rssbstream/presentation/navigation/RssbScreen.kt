package com.vishalk.rssbstream.presentation.navigation

import com.vishalk.rssbstream.data.model.ContentType

/**
 * Screen definitions for RSSB-specific navigation.
 */
sealed class RssbScreen(val route: String) {
    object RssbHome : RssbScreen("rssb_home")
    object Search : RssbScreen("rssb_search")
    object Library : RssbScreen("rssb_library")
    
    object Audiobooks : RssbScreen("rssb_audiobooks")
    object QnA : RssbScreen("rssb_qna")
    object Shabads : RssbScreen("rssb_shabads")
    object Discourses : RssbScreen("rssb_discourses")

    // General App Screens
    object Settings : RssbScreen("rssb_settings")
    object About : RssbScreen("rssb_about")
    object NavBarCornerRadius : RssbScreen("rssb_navbar_radius")
    
    // Detail routes
    object AudiobookDetail : RssbScreen("rssb_audiobook/{id}") {
        fun createRoute(id: String) = "rssb_audiobook/$id"
    }
}
