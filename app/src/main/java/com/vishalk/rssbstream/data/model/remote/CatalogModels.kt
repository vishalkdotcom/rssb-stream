package com.vishalk.rssbstream.data.model.remote

import com.google.gson.annotations.SerializedName
import com.vishalk.rssbstream.data.model.ContentType
import com.vishalk.rssbstream.data.model.RssbContent

/**
 * Response model for audiobook catalog JSON.
 * Maps to the structure in catalog/audiobooks.json on R2.
 */
data class AudiobookCatalogItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val language: String? = "en",
    @SerializedName("thumbnailUrl")
    val thumbnailPath: String? = null,
    val chapters: List<ChapterItem> = emptyList()
)

data class ChapterItem(
    val id: String,
    val title: String,
    val trackNumber: Int,
    val duration: Long, // seconds
    @SerializedName("streamUrl")
    val streamPath: String,
    val startTime: Long = 0,
    val endTime: Long? = null
)

/**
 * Response model for Q&A catalog JSON.
 */
data class QnaCatalogItem(
    val id: String,
    val title: String,
    val speaker: String? = "Maharaj Charan Singh",
    val duration: Long,
    @SerializedName("streamUrl")
    val streamPath: String,
    val dateAdded: Long? = null
)

/**
 * Response model for Shabad catalog JSON.
 */
data class ShabadCatalogItem(
    val id: String,
    val title: String,
    val mystic: String? = null,
    val duration: Long,
    @SerializedName("streamUrl")
    val streamPath: String,
    val hasTranscript: Boolean = false,
    val transcript: String? = null
)

/**
 * Response model for Discourse catalog JSON.
 */
data class DiscourseCatalogItem(
    val id: String,
    val title: String,
    val type: String, // "DISCOURSE_MASTER" or "DISCOURSE_DISCIPLE"
    val language: String,
    val speaker: String? = null,
    val duration: Long,
    @SerializedName("streamUrl")
    val streamPath: String
)

// Extension functions to convert catalog items to RssbContent

fun AudiobookCatalogItem.toRssbContents(): List<RssbContent> {
    return chapters.map { chapter ->
        RssbContent(
            id = chapter.id,
            title = chapter.title,
            type = ContentType.AUDIOBOOK_CHAPTER,
            language = language,
            author = null,
            description = description,
            thumbnailPath = thumbnailPath,
            duration = chapter.duration,
            streamPath = chapter.streamPath,
            parentId = id,
            trackNumber = chapter.trackNumber,
            startTime = chapter.startTime,
            endTime = chapter.endTime
        )
    }
}

fun QnaCatalogItem.toRssbContent(): RssbContent {
    return RssbContent(
        id = id,
        title = title,
        type = ContentType.QNA,
        author = speaker,
        duration = duration,
        streamPath = streamPath,
        dateAdded = dateAdded ?: System.currentTimeMillis()
    )
}

fun ShabadCatalogItem.toRssbContent(): RssbContent {
    return RssbContent(
        id = id,
        title = title,
        type = ContentType.SHABAD,
        author = mystic,
        duration = duration,
        streamPath = streamPath,
        transcript = transcript
    )
}

fun DiscourseCatalogItem.toRssbContent(): RssbContent {
    val contentType = if (type == "DISCOURSE_MASTER") {
        ContentType.DISCOURSE_MASTER
    } else {
        ContentType.DISCOURSE_DISCIPLE
    }
    
    return RssbContent(
        id = id,
        title = title,
        type = contentType,
        language = language,
        author = speaker,
        duration = duration,
        streamPath = streamPath
    )
}
