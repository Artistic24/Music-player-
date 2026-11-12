package com.vibe.music

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.security.MessageDigest

data class LyricsResult(val synced: String?, val plain: String?) {
    fun display(): String = synced ?: plain.orEmpty()
}
data class LyricLine(val timeMs: Long, val text: String)
object LyricsClient {
    fun cacheKey(track: Track): String = MessageDigest.getInstance("SHA-256").digest("${track.artist}|${track.title}|${track.durationMs}".toByteArray()).joinToString("") { "%02x".format(it) }
    fun find(track: Track): LyricsResult? {
        val artist = URLEncoder.encode(track.artist, "UTF-8"); val title = URLEncoder.encode(track.title, "UTF-8")
        val seconds = (track.durationMs / 1000).coerceAtLeast(1)
        val conn = (URL("https://lrclib.net/api/get?artist_name=$artist&track_name=$title&duration=$seconds").openConnection() as HttpURLConnection).apply { connectTimeout=8000; readTimeout=8000; setRequestProperty("User-Agent", "VIBE-Music-Player/0.2") }
        return try { if (conn.responseCode !in 200..299) return null; val j=JSONObject(conn.inputStream.bufferedReader().use { it.readText() }); LyricsResult(j.optString("syncedLyrics").takeIf { it.isNotBlank() && it!="null" }, j.optString("plainLyrics").takeIf { it.isNotBlank() && it!="null" }) } finally { conn.disconnect() }
    }
    fun parseLrc(raw: String): List<LyricLine> {
        val timestamp = Regex("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?]")
        return raw.lineSequence().flatMap { line ->
            val matches = timestamp.findAll(line).toList()
            if (matches.isEmpty()) emptySequence()
            else {
                // For repeated timestamps, every timestamp on the line shares
                // the lyric text that follows the final timestamp tag.
                val lyricText = line.substring(matches.last().range.last + 1).trim()
                matches.asSequence().map { match ->
                    val minutes = match.groupValues[1].toLong()
                    val seconds = match.groupValues[2].toLong()
                    val fraction = match.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
                    LyricLine((minutes * 60 + seconds) * 1000 + fraction, lyricText)
                }
            }
        }.sortedBy { it.timeMs }.toList()
    }
}
