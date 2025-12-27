package com.example.musicplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.player.PlayerManager
import com.example.musicplayer.ui.NavigationHost
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            lifecycleScope.launch { PlayerManager.initialize(this@MainActivity) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request runtime permission according to Android 14/15 standards
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(permission)
        } else {
            lifecycleScope.launch { PlayerManager.initialize(this@MainActivity) }
        }

        setContent {
            MusicPlayerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavigationHost()
                }
            }
        }
    }
}
