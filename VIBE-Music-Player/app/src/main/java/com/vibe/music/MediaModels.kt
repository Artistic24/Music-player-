package com.vibe.music

import android.net.Uri

data class Track(val id: Long, val title: String, val artist: String, val uri: Uri, val durationMs: Long, val dateAdded: Long, val isVideo: Boolean = false) {
    fun durationLabel(): String { val s = durationMs / 1000; return "%d:%02d".format(s / 60, s % 60) }
}
