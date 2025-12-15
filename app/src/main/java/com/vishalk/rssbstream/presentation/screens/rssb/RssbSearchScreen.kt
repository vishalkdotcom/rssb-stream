package com.vishalk.rssbstream.presentation.screens.rssb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vishalk.rssbstream.data.model.RssbContent
import com.vishalk.rssbstream.data.model.toSong
import com.vishalk.rssbstream.presentation.components.RssbContentListItem
import com.vishalk.rssbstream.presentation.viewmodel.ContentViewModel
import com.vishalk.rssbstream.presentation.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssbSearchScreen(
    navController: NavController,
    contentViewModel: ContentViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val searchQuery by contentViewModel.searchQuery.collectAsState()
    val searchResults by contentViewModel.searchResults.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            SearchBar(
                query = searchQuery,
                onQueryChange = { contentViewModel.search(it) },
                onSearch = { /* Search is reactive */ },
                active = true,
                onActiveChange = { },
                placeholder = { Text("Search content...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { contentViewModel.search("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester)
            ) {
                if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No results found for '$searchQuery'",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { content ->
                            RssbContentListItem(
                                content = content,
                                onClick = {
                                    playerViewModel.showAndPlaySong(
                                        content.toSong(),
                                        searchResults.map { it.toSong() }, // Context queue
                                        "Search: $searchQuery"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // SearchBar content handles everything, but we need this scaffold body to be safe
        Box(modifier = Modifier.padding(innerPadding))
    }
}
