package com.vishalk.rssbstream.utils

import com.vishalk.rssbstream.data.model.Song
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}

fun formatTotalDuration(songs: List<Song>): String {
    val totalMillis = songs.sumOf { it.duration }
    val hours = TimeUnit.MILLISECONDS.toHours(totalMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis) % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d h %02d min", hours, minutes)
    } else {
        String.format(Locale.ROOT, "%d min", minutes)
    }
}

fun formatListeningDurationLong(milliseconds: Long): String {
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
    return when {
        hours > 0 && minutes > 0 -> String.format(Locale.ROOT, "%d h %02d m", hours, minutes)
        hours > 0 -> String.format(Locale.ROOT, "%d h", hours)
        minutes > 0 -> String.format(Locale.ROOT, "%d m", minutes)
        else -> String.format(Locale.ROOT, "%d s", seconds)
    }
}

fun formatListeningDurationCompact(milliseconds: Long): String {
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
    return when {
        hours > 0 && minutes > 0 -> String.format(Locale.ROOT, "%dh %02dm", hours, minutes)
        hours > 0 -> String.format(Locale.ROOT, "%dh", hours)
        minutes > 0 -> String.format(Locale.ROOT, "%dm", minutes)
        else -> String.format(Locale.ROOT, "%ds", seconds)
    }
}


