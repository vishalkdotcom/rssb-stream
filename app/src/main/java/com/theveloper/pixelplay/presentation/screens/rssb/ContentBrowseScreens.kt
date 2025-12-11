package com.theveloper.pixelplay.presentation.screens.rssb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theveloper.pixelplay.data.model.Audiobook
import com.theveloper.pixelplay.data.model.RssbContent
import com.theveloper.pixelplay.presentation.navigation.RssbScreen
import com.theveloper.pixelplay.presentation.viewmodel.ContentViewModel

/**
 * Audiobooks browse screen - grid of all audiobooks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobooksScreen(
    navController: NavController,
    contentViewModel: ContentViewModel = hiltViewModel()
) {
    val audiobooks by contentViewModel.audiobooks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audiobooks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (audiobooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No audiobooks found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Sync catalogs to load content",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(audiobooks) { audiobook ->
                    AudiobookGridItem(
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

@Composable
private fun AudiobookGridItem(
    audiobook: Audiobook,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                Icons.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                audiobook.title,
                style = MaterialTheme.typography.titleSmall,
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

/**
 * Q&A Sessions browse screen - list of all sessions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QnaScreen(
    navController: NavController,
    contentViewModel: ContentViewModel = hiltViewModel()
) {
    val sessions by contentViewModel.qnaSessions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Q&A Sessions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sessions) { session ->
                ContentListItem(
                    content = session,
                    onClick = { /* Navigate to player */ }
                )
            }
        }
    }
}

/**
 * Shabads browse screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShabadsScreen(
    navController: NavController,
    contentViewModel: ContentViewModel = hiltViewModel()
) {
    val shabads by contentViewModel.shabads.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shabads") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by title or mystic...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            val filteredShabads = if (searchQuery.isBlank()) {
                shabads
            } else {
                shabads.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.author?.contains(searchQuery, ignoreCase = true) == true
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredShabads) { shabad ->
                    ContentListItem(
                        content = shabad,
                        showAuthor = true,
                        onClick = { /* Navigate to player */ }
                    )
                }
            }
        }
    }
}

/**
 * Discourses browse screen with language tabs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoursesScreen(
    navController: NavController,
    contentViewModel: ContentViewModel = hiltViewModel()
) {
    val discourses by contentViewModel.discourses.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    val languages = listOf(
        "All" to null,
        "English" to "en",
        "Hindi" to "hi",
        "Punjabi" to "pa",
        "Spanish" to "es",
        "French" to "fr"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discourses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Language tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                languages.forEachIndexed { index, (label, _) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) }
                    )
                }
            }

            val filteredDiscourses = languages[selectedTab].second?.let { lang ->
                discourses.filter { it.language == lang }
            } ?: discourses

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredDiscourses) { discourse ->
                    ContentListItem(
                        content = discourse,
                        showAuthor = true,
                        onClick = { /* Navigate to player */ }
                    )
                }
            }
        }
    }
}

/**
 * Reusable content list item.
 */
@Composable
fun ContentListItem(
    content: RssbContent,
    showAuthor: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    content.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (showAuthor && content.author != null) {
                    Text(
                        content.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (content.duration > 0) {
                    val minutes = content.duration / 60
                    Text(
                        "${minutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
