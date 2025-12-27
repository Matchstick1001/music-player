package com.example.musicplayer.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.musicplayer.MainActivity

class PlayerService : Service() {

    companion object {
        const val ACTION_START_FOREGROUND = "com.example.musicplayer.action.START_FOREGROUND"
        const val ACTION_TOGGLE_PLAY = "com.example.musicplayer.action.TOGGLE_PLAY"
        const val ACTION_STOP = "com.example.musicplayer.action.STOP"
        const val EXTRA_URI = "extra_uri"
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1337
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                val uriString = intent.getStringExtra(EXTRA_URI)
                if (!uriString.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(uriString)
                        PlayerManager.playUri(applicationContext, uri)
                    } catch (_: Exception) {}
                }
                val notification = buildNotification(isPlaying = true)
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_TOGGLE_PLAY -> {
                PlayerManager.toggle()
                // update notification state
                val notification = buildNotification(isPlaying = PlayerManager.isPlaying.value)
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIFICATION_ID, notification)
            }
            ACTION_STOP -> {
                PlayerManager.pause()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val activityPending = PendingIntent.getActivity(this, 0, activityIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0)

        val toggleIntent = Intent(this, PlayerService::class.java).apply { action = ACTION_TOGGLE_PLAY }
        val togglePending = PendingIntent.getService(this, 1, toggleIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0)

        val stopIntent = Intent(this, PlayerService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 2, stopIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText(if (isPlaying) "Playing" else "Paused")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(activityPending)
            .setOnlyAlertOnce(true)
            .addAction(if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play, if (isPlaying) "Pause" else "Play", togglePending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }
}
