package com.vishalk.rssbstream.data.model

import com.vishalk.rssbstream.data.model.RssbContent
import com.vishalk.rssbstream.data.model.Song
import com.vishalk.rssbstream.data.network.R2Config

/**
 * Extension function to convert RssbContent to Song.
 * This bridges the gap between the Remote Content Repository and the Player ViewModel.
 */
fun RssbContent.toSong(): Song {
    // Construct a full URI. If localPath exists (downloaded), use it.
    // Otherwise construct the remote URL.
    val uri = if (isDownloaded && localPath != null) {
        "file://$localPath"
    } else {
        // Fallback to construction if streamPath is relative
        if (streamPath.startsWith("http")) streamPath else "${R2Config.BASE_URL}/$streamPath"
    }

    val artworkUri = if (thumbnailPath != null) {
        if (thumbnailPath.startsWith("http")) thumbnailPath else "${R2Config.BASE_URL}/$thumbnailPath"
    } else {
        null
    }

    return Song(
        id = id,
        title = title,
        artist = author ?: "RSSB",
        artistId = -1L, // Remote content doesn't have local artist IDs
        album = "RSSB Audio", // Could be refined based on type
        albumId = -1L,
        path = uri, // Use URI as path for compatibility
        contentUriString = uri,
        albumArtUriString = artworkUri,
        duration = duration * 1000, // RssbContent duration is seconds, Song is usually ms?
                                    // Wait, Song.kt doesn't specify unit, but ExoPlayer expects ms.
                                    // RssbContent doc says seconds. So * 1000.
        genre = type.name,
        lyrics = transcript,
        isFavorite = isFavorite,
        trackNumber = trackNumber,
        year = 0,
        dateAdded = dateAdded,
        mimeType = "audio/mpeg", // Default assumption
        bitrate = 0,
        sampleRate = 0,
        startTime = startTime,
        endTime = endTime
    )
}
