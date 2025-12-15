package com.vishalk.rssbstream.presentation.screens.rssb

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vishalk.rssbstream.data.model.toSong
import com.vishalk.rssbstream.presentation.components.RssbContentListItem
import com.vishalk.rssbstream.presentation.viewmodel.ContentViewModel
import com.vishalk.rssbstream.presentation.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssbLibraryScreen(
    navController: NavController,
    contentViewModel: ContentViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Favorites", "Downloads")

    val favorites by contentViewModel.favorites.collectAsState()
    val downloadedContent by contentViewModel.downloadedContent.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Your Library",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                if (index == 0) Icons.Default.Favorite else Icons.Default.Download,
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    if (favorites.isEmpty()) {
                        EmptyState(
                            message = "No favorites yet",
                            icon = Icons.Default.Favorite
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favorites) { content ->
                                RssbContentListItem(
                                    content = content,
                                    onClick = {
                                        playerViewModel.showAndPlaySong(
                                            content.toSong(),
                                            favorites.map { it.toSong() },
                                            "Favorites"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (downloadedContent.isEmpty()) {
                        EmptyState(
                            message = "No downloads yet",
                            icon = Icons.Default.Download
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(downloadedContent) { content ->
                                RssbContentListItem(
                                    content = content,
                                    onClick = {
                                        playerViewModel.showAndPlaySong(
                                            content.toSong(),
                                            downloadedContent.map { it.toSong() },
                                            "Downloads"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
