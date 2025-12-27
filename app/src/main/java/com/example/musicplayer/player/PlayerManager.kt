package com.example.musicplayer.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    suspend fun initialize(context: Context) {
        if (exoPlayer != null) return
        exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer?.addListener(object : androidx.media3.common.Player.Listener {})
    }

    fun playUri(context: Context, uri: Uri) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        val item = MediaItem.fromUri(uri)
        exoPlayer?.setMediaItem(item)
        exoPlayer?.prepare()
        exoPlayer?.play()
        CoroutineScope(Dispatchers.Main).launch { _isPlaying.value = true }
        // ensure service runs in foreground to show controls / keep playback alive
        try {
            val intent = android.content.Intent(context, PlayerService::class.java).apply {
                action = PlayerService.ACTION_START_FOREGROUND
                putExtra(PlayerService.EXTRA_URI, uri.toString())
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            // ignore service start failures for now
        }
    }

    fun pause() {
        exoPlayer?.pause()
        CoroutineScope(Dispatchers.Main).launch { _isPlaying.value = false }
    }

    fun toggle() {
        if (exoPlayer?.isPlaying == true) pause() else exoPlayer?.play()?.let { CoroutineScope(Dispatchers.Main).launch { _isPlaying.value = true } }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
