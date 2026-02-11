package com.example.fzo.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.fzo.data.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioController(private val context: Context) {
    private val TAG = "FZO:AudioController"
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private var progressJob: Job? = null
    private var songsList: List<Song> = emptyList()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "onIsPlayingChanged: $isPlaying")
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                _durationMs.value = player.duration.coerceAtLeast(0L)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d(TAG, "onMediaItemTransition: reason=$reason")
                val index = player.currentMediaItemIndex
                if (index >= 0 && index < songsList.size) {
                    _currentSong.value = songsList[index]
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e(TAG, "onPlayerError: ${error.message}", error)
            }
        })
        startProgressUpdates()
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                _positionMs.value = player.currentPosition
                _durationMs.value = player.duration.coerceAtLeast(0L)
                delay(500L)
            }
        }
    }

    fun setPlaylist(songs: List<Song>) {
        this.songsList = songs
        player.setMediaItems(songs.map { MediaItem.fromUri(it.url) })
        player.prepare()
    }

    fun playAtIndex(index: Int) {
        if (index < 0 || index >= songsList.size) return
        Log.d(TAG, "playAtIndex: $index")
        _currentSong.value = songsList[index]
        player.seekTo(index, 0)
        player.play()
        _isPlaying.value = true
    }

    fun play() {
        Log.d(TAG, "play")
        player.play()
        _isPlaying.value = true
    }

    fun pause() {
        Log.d(TAG, "pause")
        player.pause()
        _isPlaying.value = false
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNext()
        }
    }

    fun previous() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious()
        }
    }

    fun setVolume(volume: Float) {
        player.volume = volume
    }

    fun setShuffleMode(enabled: Boolean) {
        player.shuffleModeEnabled = enabled
    }

    fun setAutoPlayAll(enabled: Boolean) {
        // enabled = true -> Continuous Play (Don't pause at end)
        // enabled = false -> Play Single (Pause at end of current song)
        player.pauseAtEndOfMediaItems = !enabled
        player.repeatMode = Player.REPEAT_MODE_OFF // User didn't want looping, just linear play
    }

    fun release() {
        progressJob?.cancel()
        scope.cancel()
        player.release()
    }
}
