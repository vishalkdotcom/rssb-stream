package com.theveloper.pixelplay.data.repository

import android.content.Context
import com.theveloper.pixelplay.data.database.RssbContentDao
import com.theveloper.pixelplay.data.model.Audiobook
import com.theveloper.pixelplay.data.model.ContentType
import com.theveloper.pixelplay.data.model.RssbContent
import com.theveloper.pixelplay.data.model.remote.toRssbContent
import com.theveloper.pixelplay.data.model.remote.toRssbContents
import com.theveloper.pixelplay.data.network.ContentCatalogApi
import com.theveloper.pixelplay.data.network.R2Config
import com.theveloper.pixelplay.data.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RemoteContentRepository.
 * Handles syncing catalogs from R2 and local caching via Room.
 */
@Singleton
class RemoteContentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentDao: RssbContentDao,
    private val catalogApi: ContentCatalogApi,
    private val okHttpClient: OkHttpClient,
    private val preferencesManager: PreferencesManager
) : RemoteContentRepository {

    companion object {
        private const val TAG = "RemoteContentRepo"
        private const val PREF_LAST_SYNC = "last_catalog_sync"
        private const val SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    // ===== Sync Operations =====

    override suspend fun syncAllCatalogs(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting catalog sync...")
            
            // Sync audiobooks
            syncAudiobooks()
            
            // Sync Q&A
            syncQnaSessions()
            
            // Sync Shabads
            syncShabads()
            
            // Sync Discourses
            syncDiscourses()
            
            // Update last sync time
            preferencesManager.putLong(PREF_LAST_SYNC, System.currentTimeMillis())
            
            Timber.d("Catalog sync complete!")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Catalog sync failed")
            Result.failure(e)
        }
    }

    override suspend fun syncCatalog(type: ContentType): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            when (type) {
                ContentType.AUDIOBOOK, ContentType.AUDIOBOOK_CHAPTER -> syncAudiobooks()
                ContentType.QNA -> syncQnaSessions()
                ContentType.SHABAD -> syncShabads()
                ContentType.DISCOURSE_MASTER, ContentType.DISCOURSE_DISCIPLE -> syncDiscourses()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync catalog for $type")
            Result.failure(e)
        }
    }

    private suspend fun syncAudiobooks() {
        val audiobooks = catalogApi.getAudiobooks()
        val allChapters = audiobooks.flatMap { it.toRssbContents() }
        contentDao.deleteByType(ContentType.AUDIOBOOK_CHAPTER)
        contentDao.insertAll(allChapters)
        Timber.d("Synced ${audiobooks.size} audiobooks with ${allChapters.size} chapters")
    }

    private suspend fun syncQnaSessions() {
        val sessions = catalogApi.getQnaSessions()
        val content = sessions.map { it.toRssbContent() }
        contentDao.deleteByType(ContentType.QNA)
        contentDao.insertAll(content)
        Timber.d("Synced ${content.size} Q&A sessions")
    }

    private suspend fun syncShabads() {
        val shabads = catalogApi.getShabads()
        val content = shabads.map { it.toRssbContent() }
        contentDao.deleteByType(ContentType.SHABAD)
        contentDao.insertAll(content)
        Timber.d("Synced ${content.size} shabads")
    }

    private suspend fun syncDiscourses() {
        val discourses = catalogApi.getDiscourses()
        val content = discourses.map { it.toRssbContent() }
        contentDao.deleteByType(ContentType.DISCOURSE_MASTER)
        contentDao.deleteByType(ContentType.DISCOURSE_DISCIPLE)
        contentDao.insertAll(content)
        Timber.d("Synced ${content.size} discourses")
    }

    // ===== Audiobooks =====

    override fun getAudiobooks(): Flow<List<Audiobook>> {
        return contentDao.getAudiobookIds().map { audiobookIds ->
            audiobookIds.mapNotNull { id ->
                val chapters = contentDao.getAllByTypeOnce(ContentType.AUDIOBOOK_CHAPTER)
                    .filter { it.parentId == id }
                    .sortedBy { it.trackNumber }
                
                if (chapters.isNotEmpty()) {
                    Audiobook(
                        id = id,
                        title = id.replace("-", " ").split(" ")
                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                        thumbnailPath = chapters.firstOrNull()?.thumbnailPath,
                        language = chapters.firstOrNull()?.language ?: "en",
                        chapterCount = chapters.size,
                        totalDuration = chapters.sumOf { it.duration },
                        chapters = chapters
                    )
                } else null
            }
        }
    }

    override fun getAudiobookChapters(audiobookId: String): Flow<List<RssbContent>> {
        return contentDao.getAudiobookChapters(audiobookId)
    }

    // ===== Q&A Sessions =====

    override fun getQnaSessions(): Flow<List<RssbContent>> {
        return contentDao.getQnaSessions()
    }

    // ===== Shabads =====

    override fun getShabads(): Flow<List<RssbContent>> {
        return contentDao.getShabads()
    }

    override fun getShabadsByMystic(mystic: String): Flow<List<RssbContent>> {
        return contentDao.getShabads().map { shabads ->
            shabads.filter { it.author?.contains(mystic, ignoreCase = true) == true }
        }
    }

    // ===== Discourses =====

    override fun getDiscourses(): Flow<List<RssbContent>> {
        return contentDao.getDiscourses()
    }

    override fun getDiscoursesByLanguage(language: String): Flow<List<RssbContent>> {
        return contentDao.getDiscoursesByLanguage(language)
    }

    override fun getDiscoursesByType(type: ContentType): Flow<List<RssbContent>> {
        return contentDao.getAllByType(type)
    }

    // ===== Search =====

    override fun searchContent(query: String): Flow<List<RssbContent>> {
        return contentDao.search(query)
    }

    override fun searchContent(query: String, type: ContentType): Flow<List<RssbContent>> {
        return contentDao.searchByType(query, type)
    }

    // ===== Single Item =====

    override fun getContentById(id: String): Flow<RssbContent?> {
        return contentDao.getById(id)
    }

    // ===== Favorites =====

    override fun getFavorites(): Flow<List<RssbContent>> {
        return contentDao.getFavorites()
    }

    override suspend fun toggleFavorite(id: String): Boolean {
        val content = contentDao.getByIdOnce(id) ?: return false
        val newFavoriteState = !content.isFavorite
        contentDao.setFavorite(id, newFavoriteState)
        return newFavoriteState
    }

    // ===== Recently Played =====

    override fun getRecentlyPlayed(): Flow<List<RssbContent>> {
        return contentDao.getRecentlyPlayed()
    }

    override suspend fun updatePlaybackPosition(id: String, positionMs: Long) {
        contentDao.updatePlaybackPosition(id, positionMs)
    }

    // ===== Offline/Download =====

    override fun getDownloadedContent(): Flow<List<RssbContent>> {
        return contentDao.getDownloadedContent()
    }

    override suspend fun downloadContent(content: RssbContent): Result<String> = withContext(Dispatchers.IO) {
        try {
            val streamUrl = getStreamUrl(content)
            val fileName = "${content.id}.mp3"
            val downloadDir = File(context.filesDir, "downloads")
            downloadDir.mkdirs()
            val outputFile = File(downloadDir, fileName)
            
            val request = Request.Builder().url(streamUrl).build()
            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }
            
            response.body?.byteStream()?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            contentDao.markAsDownloaded(content.id, outputFile.absolutePath)
            Timber.d("Downloaded: ${content.title} to ${outputFile.absolutePath}")
            
            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Timber.e(e, "Download failed for ${content.id}")
            Result.failure(e)
        }
    }

    override suspend fun removeDownload(id: String) {
        val content = contentDao.getByIdOnce(id)
        content?.localPath?.let { path ->
            File(path).delete()
        }
        contentDao.removeDownload(id)
    }

    // ===== Utilities =====

    override fun getStreamUrl(content: RssbContent): String {
        // If downloaded, return local path
        if (content.isDownloaded && content.localPath != null) {
            return content.localPath
        }
        // Otherwise return R2 URL
        return "${R2Config.BASE_URL}/${content.streamPath}"
    }

    override fun getThumbnailUrl(content: RssbContent): String? {
        return content.thumbnailPath?.let { "${R2Config.BASE_URL}/$it" }
    }

    override suspend fun needsSync(): Boolean {
        val lastSync = preferencesManager.getLong(PREF_LAST_SYNC, 0L)
        val timeSinceSync = System.currentTimeMillis() - lastSync
        
        // Need sync if never synced or stale
        if (lastSync == 0L || timeSinceSync > SYNC_INTERVAL_MS) {
            return true
        }
        
        // Also need sync if database is empty
        return contentDao.countAll() == 0
    }
}
