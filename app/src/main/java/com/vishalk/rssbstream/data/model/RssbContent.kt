package com.vishalk.rssbstream.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a piece of RSSB audio content.
 * This is the main model for all streamable content from R2.
 */
@Immutable
@Entity(tableName = "rssb_content")
data class RssbContent(
    @PrimaryKey
    val id: String,
    
    val title: String,
    
    val type: ContentType,
    
    /** Language code: en, hi, pa, es, fr, sd */
    val language: String? = null,
    
    /** Author/speaker - mystic name for shabads, speaker for discourses */
    val author: String? = null,
    
    val description: String? = null,
    
    /** Relative path to thumbnail on R2 (e.g., "thumbnails/audiobooks/xyz.jpg") */
    val thumbnailPath: String? = null,
    
    /** Duration in seconds */
    val duration: Long = 0,
    
    /** Relative path to audio file on R2 (e.g., "audio/qna/001.mp3") */
    val streamPath: String,
    
    /** Transcript text (for shabads) */
    val transcript: String? = null,
    
    /** Parent ID for chapters (audiobook ID) */
    val parentId: String? = null,
    
    /** Track/chapter number for ordering */
    val trackNumber: Int = 0,
    
    /** Unix timestamp when added to catalog */
    val dateAdded: Long = System.currentTimeMillis(),
    
    /** Whether this content is downloaded for offline */
    val isDownloaded: Boolean = false,
    
    /** Local file path if downloaded */
    val localPath: String? = null,
    
    /** Playback position in milliseconds (for resume) */
    val playbackPosition: Long = 0,
    
    /** Whether this item is marked as favorite */
    val isFavorite: Boolean = false
) {
    companion object {
        fun empty(): RssbContent = RssbContent(
            id = "",
            title = "",
            type = ContentType.QNA,
            streamPath = ""
        )
    }
}
