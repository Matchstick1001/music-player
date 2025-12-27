package com.example.musicplayer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavigationHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "all_songs", modifier = Modifier.fillMaxSize()) {
        composable("all_songs") { AllSongsScreen() }
        composable("artists") { PlaceholderScreen("Artists") }
        composable("albums") { PlaceholderScreen("Albums") }
        composable("playlists") { PlaylistsScreen() }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}
