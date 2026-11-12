package com.vibe.music

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Small durable user-state store. Media files remain in MediaStore; only VIBE metadata is stored here. */
class UserLibraryStore(context: Context) {
    private val prefs = context.getSharedPreferences("vibe_user_library", Context.MODE_PRIVATE)
    fun playCounts(): Map<Long, Int> = decodeCounts(prefs.getString("play_counts", "{}"))
    fun incrementPlay(id: Long) { val m = playCounts().toMutableMap(); m[id] = (m[id] ?: 0) + 1; prefs.edit().putString("play_counts", JSONObject().apply { m.forEach { (k,v) -> put(k.toString(),v) } }.toString()).apply() }
    fun favourites(): Set<Long> = prefs.getStringSet("favourites", emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
    fun toggleFavourite(id: Long): Set<Long> { val s = favourites().toMutableSet(); if (!s.add(id)) s.remove(id); prefs.edit().putStringSet("favourites", s.map { it.toString() }.toSet()).apply(); return s }
    fun playlists(): Map<String, Set<Long>> = try { val j=JSONObject(prefs.getString("playlists", "{}")!!); j.keys().asSequence().associateWith { k -> val a=j.getJSONArray(k); (0 until a.length()).mapNotNull { a.optLong(it).takeIf { n -> n>0 } }.toSet() } } catch (_:Exception) { emptyMap() }
    fun savePlaylist(name: String, ids: Set<Long>) { val j=JSONObject(); playlists().forEach { (k,v)-> j.put(k, JSONArray(v.toList())) }; j.put(name, JSONArray(ids.toList())); prefs.edit().putString("playlists",j.toString()).apply() }
    fun lyrics(key: String): String? = prefs.getString("lyrics_$key", null)
    fun saveLyrics(key: String, value: String) { prefs.edit().putString("lyrics_$key", value).apply() }
    companion object { private fun decodeCounts(raw:String?):Map<Long,Int> = try { val j=JSONObject(raw ?: "{}"); j.keys().asSequence().mapNotNull { k -> k.toLongOrNull()?.let { it to j.optInt(k) } }.toMap() } catch (_:Exception) { emptyMap() } }
}
