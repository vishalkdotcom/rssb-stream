package com.vishalk.rssbstream.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishalk.rssbstream.data.model.Audiobook
import com.vishalk.rssbstream.data.model.ContentType
import com.vishalk.rssbstream.data.model.RssbContent
import com.vishalk.rssbstream.data.repository.RemoteContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for RSSB content screens.
 * Manages catalog sync and content retrieval.
 */
@HiltViewModel
class ContentViewModel @Inject constructor(
    private val contentRepository: RemoteContentRepository
) : ViewModel() {

    // ===== Sync State =====
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    // ===== Content Flows =====
    
    val audiobooks: StateFlow<List<Audiobook>> = contentRepository.getAudiobooks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val qnaSessions: StateFlow<List<RssbContent>> = contentRepository.getQnaSessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val shabads: StateFlow<List<RssbContent>> = contentRepository.getShabads()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val discourses: StateFlow<List<RssbContent>> = contentRepository.getDiscourses()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val favorites: StateFlow<List<RssbContent>> = contentRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val recentlyPlayed: StateFlow<List<RssbContent>> = contentRepository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val downloadedContent: StateFlow<List<RssbContent>> = contentRepository.getDownloadedContent()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ===== Search =====
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<RssbContent>>(emptyList())
    val searchResults: StateFlow<List<RssbContent>> = _searchResults.asStateFlow()

    // ===== Init =====
    
    init {
        checkAndSync()
    }

    fun checkAndSync() {
        viewModelScope.launch {
            if (contentRepository.needsSync()) {
                syncCatalogs()
            }
        }
    }

    fun syncCatalogs() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            
            contentRepository.syncAllCatalogs()
                .onSuccess {
                    Timber.d("Catalog sync successful")
                }
                .onFailure { error ->
                    Timber.e(error, "Catalog sync failed")
                    _syncError.value = error.message ?: "Sync failed"
                }
            
            _isSyncing.value = false
        }
    }

    // ===== Content Operations =====
    
    fun getAudiobookChapters(audiobookId: String): StateFlow<List<RssbContent>> {
        return contentRepository.getAudiobookChapters(audiobookId)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }
    
    fun getDiscoursesByLanguage(language: String): StateFlow<List<RssbContent>> {
        return contentRepository.getDiscoursesByLanguage(language)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            contentRepository.searchContent(query).collect { results ->
                _searchResults.value = results
            }
        }
    }

    fun toggleFavorite(contentId: String) {
        viewModelScope.launch {
            contentRepository.toggleFavorite(contentId)
        }
    }

    fun updatePlaybackPosition(contentId: String, positionMs: Long) {
        viewModelScope.launch {
            contentRepository.updatePlaybackPosition(contentId, positionMs)
        }
    }

    fun downloadContent(content: RssbContent) {
        viewModelScope.launch {
            contentRepository.downloadContent(content)
                .onSuccess { path ->
                    Timber.d("Downloaded ${content.title} to $path")
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to download ${content.title}")
                }
        }
    }

    fun removeDownload(contentId: String) {
        viewModelScope.launch {
            contentRepository.removeDownload(contentId)
        }
    }

    // ===== Utility =====
    
    fun getStreamUrl(content: RssbContent): String {
        return contentRepository.getStreamUrl(content)
    }

    fun getThumbnailUrl(content: RssbContent): String? {
        return contentRepository.getThumbnailUrl(content)
    }

    fun clearSyncError() {
        _syncError.value = null
    }
}
