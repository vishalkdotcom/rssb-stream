package com.vishalk.rssbstream.presentation.screens.rssb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vishalk.rssbstream.data.model.Audiobook
import com.vishalk.rssbstream.data.model.RssbContent
import com.vishalk.rssbstream.data.model.toSong
import com.vishalk.rssbstream.presentation.navigation.RssbScreen
import com.vishalk.rssbstream.presentation.viewmodel.ContentViewModel
import com.vishalk.rssbstream.presentation.viewmodel.PlayerViewModel

/**
 * Main home screen for RSSB Stream app.
 * Displays content categories: Audiobooks, Q&A, Shabads, Discourses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssbHomeScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    contentViewModel: ContentViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val isSyncing by contentViewModel.isSyncing.collectAsState()
    val syncError by contentViewModel.syncError.collectAsState()
    val audiobooks by contentViewModel.audiobooks.collectAsState()
    val qnaSessions by contentViewModel.qnaSessions.collectAsState()
    val shabads by contentViewModel.shabads.collectAsState()
    val recentlyPlayed by contentViewModel.recentlyPlayed.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "RSSB Stream",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { contentViewModel.syncCatalogs() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Error Banner
            if (syncError != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                syncError ?: "Sync failed",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { contentViewModel.clearSyncError() }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }

            // Recently Played
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    ContentSection(
                        title = "Continue Listening",
                        icon = Icons.Outlined.PlayCircle,
                        onSeeAllClick = { /* Navigate to recently played */ }
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(recentlyPlayed.take(5)) { content ->
                                RecentContentCard(
                                    content = content,
                                    onClick = {
                                        playerViewModel.playSongs(
                                            listOf(content.toSong()),
                                            content.toSong(),
                                            "Recently Played"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Category Grid
            item {
                Text(
                    "Browse",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        title = "Audiobooks",
                        subtitle = "${audiobooks.size} books",
                        icon = Icons.Outlined.MenuBook,
                        gradientColors = listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2)
                        ),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(RssbScreen.Audiobooks.route) }
                    )
                    CategoryCard(
                        title = "Q&A",
                        subtitle = "${qnaSessions.size} sessions",
                        icon = Icons.Outlined.QuestionAnswer,
                        gradientColors = listOf(
                            Color(0xFFf093fb),
                            Color(0xFFf5576c)
                        ),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(RssbScreen.QnA.route) }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        title = "Shabads",
                        subtitle = "${shabads.size} poems",
                        icon = Icons.Outlined.MusicNote,
                        gradientColors = listOf(
                            Color(0xFF4facfe),
                            Color(0xFF00f2fe)
                        ),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(RssbScreen.Shabads.route) }
                    )
                    CategoryCard(
                        title = "Discourses",
                        subtitle = "By Masters",
                        icon = Icons.Outlined.Mic,
                        gradientColors = listOf(
                            Color(0xFFfa709a),
                            Color(0xFFfee140)
                        ),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(RssbScreen.Discourses.route) }
                    )
                }
            }

            // Audiobooks Section
            if (audiobooks.isNotEmpty()) {
                item {
                    ContentSection(
                        title = "Audiobooks",
                        icon = Icons.Outlined.MenuBook,
                        onSeeAllClick = { navController.navigate(RssbScreen.Audiobooks.route) }
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(audiobooks.take(5)) { audiobook ->
                                AudiobookCard(
                                    audiobook = audiobook,
                                    onClick = {
                                        navController.navigate(
                                            RssbScreen.AudiobookDetail.createRoute(audiobook.id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Q&A Section
            if (qnaSessions.isNotEmpty()) {
                item {
                    ContentSection(
                        title = "Q&A Sessions",
                        icon = Icons.Outlined.QuestionAnswer,
                        onSeeAllClick = { navController.navigate(RssbScreen.QnA.route) }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            qnaSessions.take(3).forEach { session ->
                                QnaSessionItem(
                                    content = session,
                                    onClick = {
                                        playerViewModel.playSongs(
                                            listOf(session.toSong()),
                                            session.toSong(),
                                            "Q&A Sessions"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Spacer
            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun ContentSection(
    title: String,
    icon: ImageVector,
    onSeeAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = onSeeAllClick) {
                Text("See All")
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AudiobookCard(
    audiobook: Audiobook,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Placeholder for cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    audiobook.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${audiobook.chapterCount} chapters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentContentCard(
    content: RssbContent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    content.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    content.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QnaSessionItem(
    content: RssbContent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.QuestionAnswer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    content.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                content.author?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.PlayCircle,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
