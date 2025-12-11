package com.theveloper.pixelplay.data.model

import androidx.compose.runtime.Immutable

/**
 * Represents an audiobook with its chapters.
 * Used for displaying audiobook cards and navigation.
 */
@Immutable
data class Audiobook(
    val id: String,
    val title: String,
    val description: String? = null,
    val thumbnailPath: String? = null,
    val language: String = "en",
    val chapterCount: Int = 0,
    val totalDuration: Long = 0, // Total duration in seconds
    val chapters: List<RssbContent> = emptyList()
) {
    companion object {
        /**
         * Create an Audiobook from a list of its chapters.
         */
        fun fromChapters(chapters: List<RssbContent>): Audiobook? {
            if (chapters.isEmpty()) return null
            
            val firstChapter = chapters.first()
            val parentId = firstChapter.parentId ?: return null
            
            return Audiobook(
                id = parentId,
                title = firstChapter.parentId ?: "Unknown Audiobook",
                thumbnailPath = firstChapter.thumbnailPath,
                language = firstChapter.language ?: "en",
                chapterCount = chapters.size,
                totalDuration = chapters.sumOf { it.duration },
                chapters = chapters.sortedBy { it.trackNumber }
            )
        }
    }
}
