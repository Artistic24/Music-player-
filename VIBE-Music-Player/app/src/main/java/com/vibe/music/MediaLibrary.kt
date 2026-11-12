package com.vibe.music

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

object MediaLibrary {
    fun audio(c: Context): List<Track> = query(c, false)
    fun videos(c: Context): List<Track> = query(c, true)
    private fun query(c: Context, video: Boolean): List<Track> {
        val collection = if (video) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val idCol = MediaStore.MediaColumns._ID
        val titleCol = MediaStore.MediaColumns.TITLE
        val durationCol = MediaStore.MediaColumns.DURATION
        val dateCol = MediaStore.MediaColumns.DATE_ADDED
        val artistCol = MediaStore.Audio.AudioColumns.ARTIST
        val projection = if (video) arrayOf(idCol, titleCol, durationCol, dateCol) else arrayOf(idCol, titleCol, artistCol, durationCol, dateCol)
        val result = mutableListOf<Track>()
        val selection = if (video) null else "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val order = if (video) "$dateCol DESC" else "$titleCol COLLATE NOCASE ASC"
        c.contentResolver.query(collection, projection, selection, null, order)?.use { cur ->
            val id = cur.getColumnIndexOrThrow(idCol); val title = cur.getColumnIndexOrThrow(titleCol)
            val duration = cur.getColumnIndexOrThrow(durationCol); val date = cur.getColumnIndexOrThrow(dateCol)
            val artist = if (video) -1 else cur.getColumnIndexOrThrow(artistCol)
            while (cur.moveToNext()) {
                val mediaId = cur.getLong(id)
                result += Track(mediaId, cur.getString(title) ?: "Untitled", if (video) "Video file" else (cur.getString(artist)?.takeUnless { it == "<unknown>" } ?: "Unknown artist"), ContentUris.withAppendedId(collection, mediaId), cur.getLong(duration), cur.getLong(date), video)
            }
        }
        return result
    }
}
