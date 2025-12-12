package com.vishalk.rssbstream.data.repository

import com.vishalk.rssbstream.data.model.Lyrics
import com.vishalk.rssbstream.data.model.Song

interface LyricsRepository {
    suspend fun getLyrics(song: Song): Lyrics?
    suspend fun fetchFromRemote(song: Song): Result<Pair<Lyrics, String>>
    suspend fun searchRemote(song: Song): Result<Pair<String, List<LyricsSearchResult>>>
    suspend fun updateLyrics(songId: Long, lyricsContent: String)
    suspend fun resetLyrics(songId: Long)
    suspend fun resetAllLyrics()
    fun clearCache()
}