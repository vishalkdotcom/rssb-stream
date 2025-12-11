package com.theveloper.pixelplay.data.repository

import com.theveloper.pixelplay.data.model.Audiobook
import com.theveloper.pixelplay.data.model.ContentType
import com.theveloper.pixelplay.data.model.RssbContent
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing RSSB content from R2 and local cache.
 */
interface RemoteContentRepository {
    
    // ===== Sync Operations =====
    
    /**
     * Sync all content catalogs from R2 to local database.
     * Call this on app startup or manual refresh.
     */
    suspend fun syncAllCatalogs(): Result<Unit>
    
    /**
     * Sync a specific content type catalog.
     */
    suspend fun syncCatalog(type: ContentType): Result<Unit>
    
    // ===== Audiobooks =====
    
    /**
     * Get all audiobooks with their chapter counts.
     */
    fun getAudiobooks(): Flow<List<Audiobook>>
    
    /**
     * Get chapters for a specific audiobook.
     */
    fun getAudiobookChapters(audiobookId: String): Flow<List<RssbContent>>
    
    // ===== Q&A Sessions =====
    
    /**
     * Get all Q&A sessions.
     */
    fun getQnaSessions(): Flow<List<RssbContent>>
    
    // ===== Shabads =====
    
    /**
     * Get all shabads.
     */
    fun getShabads(): Flow<List<RssbContent>>
    
    /**
     * Get shabads filtered by mystic/author.
     */
    fun getShabadsByMystic(mystic: String): Flow<List<RssbContent>>
    
    // ===== Discourses =====
    
    /**
     * Get all discourses.
     */
    fun getDiscourses(): Flow<List<RssbContent>>
    
    /**
     * Get discourses by language.
     */
    fun getDiscoursesByLanguage(language: String): Flow<List<RssbContent>>
    
    /**
     * Get discourses by type (master or disciple).
     */
    fun getDiscoursesByType(type: ContentType): Flow<List<RssbContent>>
    
    // ===== Search =====
    
    /**
     * Search all content.
     */
    fun searchContent(query: String): Flow<List<RssbContent>>
    
    /**
     * Search within a specific content type.
     */
    fun searchContent(query: String, type: ContentType): Flow<List<RssbContent>>
    
    // ===== Single Item =====
    
    /**
     * Get a single content item by ID.
     */
    fun getContentById(id: String): Flow<RssbContent?>
    
    // ===== Favorites =====
    
    /**
     * Get all favorite content.
     */
    fun getFavorites(): Flow<List<RssbContent>>
    
    /**
     * Toggle favorite status for a content item.
     */
    suspend fun toggleFavorite(id: String): Boolean
    
    // ===== Recently Played =====
    
    /**
     * Get recently played content.
     */
    fun getRecentlyPlayed(): Flow<List<RssbContent>>
    
    /**
     * Update playback position for resume functionality.
     */
    suspend fun updatePlaybackPosition(id: String, positionMs: Long)
    
    // ===== Offline/Download =====
    
    /**
     * Get all downloaded content.
     */
    fun getDownloadedContent(): Flow<List<RssbContent>>
    
    /**
     * Download content for offline playback.
     * Returns the local file path on success.
     */
    suspend fun downloadContent(content: RssbContent): Result<String>
    
    /**
     * Remove downloaded content.
     */
    suspend fun removeDownload(id: String)
    
    // ===== Utilities =====
    
    /**
     * Get the full streaming URL for a content item.
     */
    fun getStreamUrl(content: RssbContent): String
    
    /**
     * Get the full thumbnail URL for a content item.
     */
    fun getThumbnailUrl(content: RssbContent): String?
    
    /**
     * Check if catalogs need to be synced (e.g., first run or stale).
     */
    suspend fun needsSync(): Boolean
}
