package com.example.fzo.viewmodel

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fzo.data.Song
import com.example.fzo.data.SettingsRepository
import com.example.fzo.player.AudioController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioViewModel(private val context: Context) : ViewModel() {
    private val controller = AudioController(context)
    private val settingsRepository = SettingsRepository(context.applicationContext)

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlistFlow: StateFlow<List<Song>> = _playlist.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentSong: StateFlow<Song?> = controller.currentSong
    val isPlaying: StateFlow<Boolean> = controller.isPlaying
    val positionMs: StateFlow<Long> = controller.positionMs
    val durationMs: StateFlow<Long> = controller.durationMs

    private var currentIndex = 0

    fun refreshSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val songs = loadSongsFromDevice()
            withContext(Dispatchers.Main) {
                _playlist.value = songs
                controller.setPlaylist(songs)
                if (songs.isNotEmpty()) {
                    val settings = settingsRepository.load()
                    controller.setVolume(settings.volume)
                    controller.setShuffleMode(settings.shuffleEnabled)
                    controller.setAutoPlayAll(settings.autoPlayAll)
                }
                _isLoading.value = false
            }
        }
    }

    fun playSong(song: Song) {
        val list = _playlist.value
        val index = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        controller.playAtIndex(index)
    }

    fun togglePlayPause() {
        if (isPlaying.value) pause() else play()
    }

    fun play() = controller.play()
    fun pause() = controller.pause()

    fun next() = controller.next()
    fun previous() = controller.previous()

    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)

    fun applyShuffle(enabled: Boolean) {
        controller.setShuffleMode(enabled)
    }

    fun applyAutoPlayAll(enabled: Boolean) {
        controller.setAutoPlayAll(enabled)
    }

    fun applyVolume(volume: Float) {
        controller.setVolume(volume)
    }

    private fun hasAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun loadSongsFromDevice(): List<Song> {
        if (!hasAudioPermission()) return emptyList()

        val resolver = context.contentResolver
        val audioCollection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val songs = mutableListOf<Song>()

        resolver.query(audioCollection, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown title"
                val artist = cursor.getString(artistColumn) ?: "Unknown artist"
                val contentUri = ContentUris.withAppendedId(audioCollection, id)

                songs.add(
                    Song(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        url = contentUri.toString(),
                        coverUrl = ""
                    )
                )
            }
        }

        return songs
    }

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }
}

class AudioViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
