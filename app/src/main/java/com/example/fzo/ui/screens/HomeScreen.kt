package com.example.fzo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fzo.AudioViewModel
import com.example.fzo.ui.components.LoadingAnimation
import com.example.fzo.ui.components.SongItem

@Composable
fun HomeScreen(
    audioViewModel: AudioViewModel,
    modifier: Modifier = Modifier
) {
    val playlist by audioViewModel.playlistFlow.collectAsState()
    val currentSong by audioViewModel.currentSong.collectAsState()
    val isLoading by audioViewModel.isLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your Music",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                // Show loading animation while songs are being loaded
                LoadingAnimation(
                    modifier = Modifier.weight(1f)
                )
            }
            playlist.isEmpty() -> {
                // Show empty state when no songs found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                // Show song list with overscroll effect
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playlist) { song ->
                        SongItem(
                            song = song,
                            isCurrentSong = song.id == currentSong?.id,
                            onClick = { audioViewModel.playSong(song) }
                        )
                    }
                    
                    // Bottom padding for mini player
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
