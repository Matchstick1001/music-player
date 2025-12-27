package com.example.musicplayer.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musicplayer.data.MediaRepository
import com.example.musicplayer.player.PlayerManager
import kotlinx.coroutines.launch

@Composable
fun AllSongsScreen() {
    val context = LocalContext.current
    val repo = remember { MediaRepository(context) }
    var songs by remember { mutableStateOf(listOf<com.example.musicplayer.data.Song>()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        songs = repo.queryAllSongs()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(songs) { song ->
                SongCard(song.title, song.artist ?: "Unknown") {
                    try {
                        PlayerManager.playUri(context, song.uri)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Unable to play: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        NowPlayingBar()
    }
}

@Composable
fun SongCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            AsyncImage(model = null, contentDescription = null, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun NowPlayingBar() {
    val isPlaying by PlayerManager.isPlaying.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    Surface(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            IconButton(onClick = { PlayerManager.toggle() }) {
                Icon(imageVector = if (isPlaying) androidx.compose.material.icons.Icons.Default.Pause else androidx.compose.material.icons.Icons.Default.PlayArrow, contentDescription = "PlayPause")
            }
            LinearProgressIndicator(progress = 0.0f, modifier = Modifier.weight(1f).height(6.dp))
        }
    }
}
