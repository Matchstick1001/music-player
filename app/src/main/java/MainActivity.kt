package com.example.fzo

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fzo.ui.MainScreen
import com.example.fzo.viewmodel.AudioViewModel
import com.example.fzo.viewmodel.AudioViewModelFactory
import com.example.fzo.viewmodel.SettingsViewModel
import com.example.fzo.viewmodel.SettingsViewModelFactory

// --- Application class ---
class FzoApplication : Application()

// --- Activity & Compose UI ---

class MainActivity : ComponentActivity() {

    private val audioViewModel: AudioViewModel by viewModels { AudioViewModelFactory(this) }
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModelFactory(this) }

    private val TAG = "FZO:MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        
        ensureAudioPermissionAndLoad()
        
        setContent {
            MainScreen(
                audioViewModel = audioViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
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
