package com.example.musicplayer.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<Long>
)

class PlaylistRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)

    fun loadAll(): List<Playlist> {
        val raw = prefs.getString("playlists_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = mutableListOf<Playlist>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val id = o.getString("id")
                val name = o.getString("name")
                val ids = mutableListOf<Long>()
                val idArr = o.optJSONArray("songIds") ?: JSONArray()
                for (j in 0 until idArr.length()) ids += idArr.getLong(j)
                out += Playlist(id, name, ids)
            }
            out
        } catch (e: Exception) { emptyList() }
    }

    fun saveAll(playlists: List<Playlist>) {
        val arr = JSONArray()
        playlists.forEach { p ->
            val o = JSONObject()
            o.put("id", p.id)
            o.put("name", p.name)
            val ids = JSONArray()
            p.songIds.forEach { ids.put(it) }
            o.put("songIds", ids)
            arr.put(o)
        }
        prefs.edit().putString("playlists_json", arr.toString()).apply()
    }

    fun addPlaylist(playlist: Playlist) {
        val current = loadAll().toMutableList()
        current += playlist
        saveAll(current)
    }

    fun removePlaylist(id: String) {
        val current = loadAll().filterNot { it.id == id }
        saveAll(current)
    }
}
