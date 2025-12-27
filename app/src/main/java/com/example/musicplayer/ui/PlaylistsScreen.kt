package com.example.musicplayer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.musicplayer.data.PlaylistRepository
import com.example.musicplayer.data.Playlist
import java.util.*

@Composable
fun PlaylistsScreen() {
    val ctx = LocalContext.current
    val repo = remember { PlaylistRepository(ctx) }
    var playlists by remember { mutableStateOf(listOf<Playlist>()) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { playlists = repo.loadAll() }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("New playlist") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (newName.isNotBlank()) {
                val p = Playlist(UUID.randomUUID().toString(), newName.trim(), emptyList())
                repo.addPlaylist(p)
                playlists = repo.loadAll()
                newName = ""
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Create") }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(playlists) { p ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.name, style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { repo.removePlaylist(p.id); playlists = repo.loadAll() }) { Text("Delete") }
                    }
                }
            }
        }
    }
}
