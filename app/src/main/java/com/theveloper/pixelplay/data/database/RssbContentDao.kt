package com.theveloper.pixelplay.data.database

import androidx.room.*
import com.theveloper.pixelplay.data.model.ContentType
import com.theveloper.pixelplay.data.model.RssbContent
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for RSSB content stored in Room database.
 * Handles local caching and offline content.
 */
@Dao
interface RssbContentDao {
    
    // ===== Insert Operations =====
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(content: List<RssbContent>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: RssbContent)
    
    // ===== Query by Type =====
    
    @Query("SELECT * FROM rssb_content WHERE type = :type ORDER BY title ASC")
    fun getAllByType(type: ContentType): Flow<List<RssbContent>>
    
    @Query("SELECT * FROM rssb_content WHERE type = :type ORDER BY title ASC")
    suspend fun getAllByTypeOnce(type: ContentType): List<RssbContent>
    
    @Query("SELECT * FROM rssb_content WHERE type = 'QNA' ORDER BY trackNumber ASC")
    fun getQnaSessions(): Flow<List<RssbContent>>
    
    @Query("SELECT * FROM rssb_content WHERE type = 'SHABAD' ORDER BY title ASC")
    fun getShabads(): Flow<List<RssbContent>>
    
    @Query("SELECT * FROM rssb_content WHERE type IN ('DISCOURSE_MASTER', 'DISCOURSE_DISCIPLE') ORDER BY title ASC")
    fun getDiscourses(): Flow<List<RssbContent>>
    
    @Query("SELECT * FROM rssb_content WHERE type IN ('DISCOURSE_MASTER', 'DISCOURSE_DISCIPLE') AND language = :language ORDER BY title ASC")
    fun getDiscoursesByLanguage(language: String): Flow<List<RssbContent>>
    
    // ===== Audiobook Queries =====
    
    @Query("SELECT * FROM rssb_content WHERE type = 'AUDIOBOOK_CHAPTER' AND parentId = :audiobookId ORDER BY trackNumber ASC")
    fun getAudiobookChapters(audiobookId: String): Flow<List<RssbContent>>
    
    @Query("SELECT DISTINCT parentId FROM rssb_content WHERE type = 'AUDIOBOOK_CHAPTER' AND parentId IS NOT NULL")
    fun getAudiobookIds(): Flow<List<String>>
    
    // ===== Single Item =====
    
    @Query("SELECT * FROM rssb_content WHERE id = :id")
    fun getById(id: String): Flow<RssbContent?>
    
    @Query("SELECT * FROM rssb_content WHERE id = :id")
    suspend fun getByIdOnce(id: String): RssbContent?
    
    // ===== Search =====
    
    @Query("SELECT * FROM rssb_content WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY title ASC LIMIT 50")
    fun search(query: String): Flow<List<RssbContent>>
    
    @Query("SELECT * FROM rssb_content WHERE type = :type AND (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%') ORDER BY title ASC LIMIT 50")
    fun searchByType(query: String, type: ContentType): Flow<List<RssbContent>>
    
    // ===== Favorites =====
    
    @Query("SELECT * FROM rssb_content WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<RssbContent>>
    
    @Query("UPDATE rssb_content SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)
    
    // ===== Offline/Downloaded =====
    
    @Query("SELECT * FROM rssb_content WHERE isDownloaded = 1 ORDER BY title ASC")
    fun getDownloadedContent(): Flow<List<RssbContent>>
    
    @Query("UPDATE rssb_content SET isDownloaded = 1, localPath = :localPath WHERE id = :id")
    suspend fun markAsDownloaded(id: String, localPath: String)
    
    @Query("UPDATE rssb_content SET isDownloaded = 0, localPath = NULL WHERE id = :id")
    suspend fun removeDownload(id: String)
    
    // ===== Playback Position =====
    
    @Query("UPDATE rssb_content SET playbackPosition = :position WHERE id = :id")
    suspend fun updatePlaybackPosition(id: String, position: Long)
    
    @Query("SELECT * FROM rssb_content WHERE playbackPosition > 0 ORDER BY dateAdded DESC LIMIT 20")
    fun getRecentlyPlayed(): Flow<List<RssbContent>>
    
    // ===== Delete Operations =====
    
    @Query("DELETE FROM rssb_content WHERE type = :type")
    suspend fun deleteByType(type: ContentType)
    
    @Query("DELETE FROM rssb_content")
    suspend fun deleteAll()
    
    // ===== Count =====
    
    @Query("SELECT COUNT(*) FROM rssb_content WHERE type = :type")
    suspend fun countByType(type: ContentType): Int
    
    @Query("SELECT COUNT(*) FROM rssb_content")
    suspend fun countAll(): Int
}
