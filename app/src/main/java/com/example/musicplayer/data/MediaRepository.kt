package com.example.musicplayer.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val uri: Uri
)

class MediaRepository(private val context: Context) {

    fun queryAllSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )

        val selection = ("${MediaStore.Audio.Media.IS_MUSIC}=1")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cursor: Cursor? = context.contentResolver.query(queryUri, projection, selection, null, sortOrder)
        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationIdx = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val id = it.getLong(idIdx)
                val title = it.getString(titleIdx) ?: "Unknown"
                val artist = it.getString(artistIdx)
                val album = it.getString(albumIdx)
                val duration = it.getLong(durationIdx)
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                songs += Song(id, title, artist, album, duration, uri)
            }
        }
        return songs
    }
}
