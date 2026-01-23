package com.example.fzo

import android.Manifest
import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// --- Application class ---
class FzoApplication : Application()

// --- Models ---
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val coverUrl: String
)

data class AppSettings(
    val autoPlay: Boolean = false,
    val loopTrack: Boolean = false,
    val volume: Float = 1f
)

// --- Settings repository using SharedPreferences ---
class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("fzo_settings", Context.MODE_PRIVATE)

    fun load(): AppSettings {
        val autoPlay = prefs.getBoolean("autoPlay", false)
        val loopTrack = prefs.getBoolean("loopTrack", false)
        val volume = prefs.getFloat("volume", 1f)
        return AppSettings(autoPlay, loopTrack, volume)
    }

    fun save(settings: AppSettings) {
        prefs.edit()
            .putBoolean("autoPlay", settings.autoPlay)
            .putBoolean("loopTrack", settings.loopTrack)
            .putFloat("volume", settings.volume)
            .apply()
    }
}

// --- Audio controller using ExoPlayer ---
class AudioController(private val context: Context) {
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

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _durationMs.value = player.duration.coerceAtLeast(0L)
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

    fun playSong(song: Song) {
        _currentSong.value = song
        player.setMediaItem(MediaItem.fromUri(song.url))
        player.prepare()
        player.play()
        _isPlaying.value = true
    }

    fun play() {
        player.play()
        _isPlaying.value = true
    }

    fun pause() {
        player.pause()
        _isPlaying.value = false
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun setVolume(volume: Float) {
        player.volume = volume
    }

    fun setLoopMode(loop: Boolean) {
        player.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun release() {
        progressJob?.cancel()
        scope.cancel()
        player.release()
    }
}

// --- ViewModels ---
class AudioViewModel(private val context: Context) : ViewModel() {
    private val controller = AudioController(context)
    private val settingsRepository = SettingsRepository(context.applicationContext)

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlistFlow: StateFlow<List<Song>> = _playlist.asStateFlow()

    val currentSong: StateFlow<Song?> = controller.currentSong
    val isPlaying: StateFlow<Boolean> = controller.isPlaying
    val positionMs: StateFlow<Long> = controller.positionMs
    val durationMs: StateFlow<Long> = controller.durationMs

    private var currentIndex = 0

    fun refreshSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val songs = loadSongsFromDevice()
            _playlist.value = songs
            if (songs.isNotEmpty()) {
                val settings = settingsRepository.load()
                controller.setVolume(settings.volume)
                controller.setLoopMode(settings.loopTrack)
                if (settings.autoPlay) {
                    playSong(songs[0])
                }
            }
        }
    }

    fun playSong(song: Song) {
        val list = _playlist.value
        currentIndex = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        controller.playSong(song)
    }

    fun togglePlayPause() {
        if (isPlaying.value) pause() else play()
    }

    fun play() = controller.play()
    fun pause() = controller.pause()

    fun next() {
        val list = _playlist.value
        if (list.isEmpty()) return
        currentIndex = (currentIndex + 1) % list.size
        playSong(list[currentIndex])
    }

    fun previous() {
        val list = _playlist.value
        if (list.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1
        playSong(list[currentIndex])
    }

    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)

    fun applyLoop(loop: Boolean) {
        controller.setLoopMode(loop)
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

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {
    private val _settings = MutableStateFlow(repo.load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setAutoPlay(value: Boolean) {
        update { it.copy(autoPlay = value) }
    }

    fun setLoopTrack(value: Boolean) {
        update { it.copy(loopTrack = value) }
    }

    fun setVolume(value: Float) {
        update { it.copy(volume = value) }
    }

    private fun update(block: (AppSettings) -> AppSettings) {
        val newSettings = block(_settings.value)
        _settings.value = newSettings
        repo.save(newSettings)
    }
}

// --- ViewModel factories ---
class AudioViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(SettingsRepository(context.applicationContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- Activity & simple UI wiring ---

class MainActivity : AppCompatActivity() {

    private val audioViewModel: AudioViewModel by viewModels { AudioViewModelFactory(this) }
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "FZO"

        // Bottom navigation between Home and Settings
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, HomeFragment.newInstance())
                        .commit()
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, SettingsFragment())
                        .addToBackStack("settings")
                        .commit()
                    true
                }
                else -> false
            }
        }

        ensureAudioPermissionAndLoad()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance())
                .commitNow()
        }
    }

    private fun ensureAudioPermissionAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            audioViewModel.refreshSongs()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_AUDIO_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            audioViewModel.refreshSongs()
        }
    }

    companion object {
        private const val REQUEST_AUDIO_PERMISSION = 1001
    }
}

// UI fragments are defined in separate files (see other Kotlin files in this module).
