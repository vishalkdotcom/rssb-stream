package com.theveloper.pixelplay.data.model

/**
 * Types of content available in RSSB Stream.
 */
enum class ContentType {
    AUDIOBOOK,      // Multi-chapter book readings
    AUDIOBOOK_CHAPTER, // Individual chapter of an audiobook
    QNA,            // Q&A sessions with Maharaj Charan Singh
    SHABAD,         // Spiritual poems/songs
    DISCOURSE_MASTER,   // Discourses by Masters (translated)
    DISCOURSE_DISCIPLE  // Discourses by Disciples
}
