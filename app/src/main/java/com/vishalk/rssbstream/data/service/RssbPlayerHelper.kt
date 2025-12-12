package com.vishalk.rssbstream.data.service

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.vishalk.rssbstream.data.model.RssbContent
import com.vishalk.rssbstream.data.network.R2Config

/**
 * Helper object to convert RSSB content to Media3 MediaItems for playback.
 * Bridges RssbContent with the existing MusicService/ExoPlayer infrastructure.
 */
object RssbPlayerHelper {

    /**
     * Convert a single RssbContent item to a Media3 MediaItem for playback.
     */
    fun toMediaItem(content: RssbContent): MediaItem {
        val streamUrl = getStreamUrl(content)
        
        val metadata = MediaMetadata.Builder()
            .setTitle(content.title)
            .setArtist(content.author ?: getDefaultArtist(content))
            .setAlbumTitle(content.parentId) // For audiobook chapters
            .setTrackNumber(content.trackNumber)
            .setDescription(content.description)
            .apply {
                content.thumbnailPath?.let {
                    setArtworkUri(Uri.parse("${R2Config.BASE_URL}/$it"))
                }
            }
            .build()

        return MediaItem.Builder()
            .setMediaId(content.id)
            .setUri(streamUrl)
            .setMediaMetadata(metadata)
            .build()
    }

    /**
     * Convert a list of RssbContent items to MediaItems.
     * Useful for playing audiobook chapters or Q&A session lists.
     */
    fun toMediaItems(contents: List<RssbContent>): List<MediaItem> {
        return contents.map { toMediaItem(it) }
    }

    /**
     * Get the streaming URL for content.
     * Uses local path if downloaded, otherwise R2 URL.
     */
    fun getStreamUrl(content: RssbContent): String {
        return if (content.isDownloaded && content.localPath != null) {
            content.localPath
        } else {
            "${R2Config.BASE_URL}/${content.streamPath}"
        }
    }

    /**
     * Get default artist/author based on content type.
     */
    private fun getDefaultArtist(content: RssbContent): String {
        return when (content.type) {
            com.vishalk.rssbstream.data.model.ContentType.QNA -> "Maharaj Charan Singh"
            com.vishalk.rssbstream.data.model.ContentType.DISCOURSE_MASTER -> "Master"
            com.vishalk.rssbstream.data.model.ContentType.DISCOURSE_DISCIPLE -> "Disciple"
            com.vishalk.rssbstream.data.model.ContentType.SHABAD -> content.author ?: "Unknown Mystic"
            else -> "RSSB"
        }
    }

    /**
     * Create a queue of MediaItems starting from a specific content item.
     * Useful for "play from here" functionality.
     */
    fun createQueueFromIndex(
        contents: List<RssbContent>,
        startIndex: Int
    ): Pair<List<MediaItem>, Int> {
        val mediaItems = toMediaItems(contents)
        return mediaItems to startIndex.coerceIn(0, mediaItems.size - 1)
    }
}
