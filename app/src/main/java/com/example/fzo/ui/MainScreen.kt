package com.example.fzo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fzo.AudioViewModel
import com.example.fzo.SettingsViewModel
import com.example.fzo.ui.components.MiniPlayer
import com.example.fzo.ui.navigation.Screen
import com.example.fzo.ui.navigation.bottomNavItems
import com.example.fzo.ui.screens.*
import com.example.fzo.ui.theme.MoodyLazyTheme
import com.example.fzo.ui.theme.FredokaOneFont
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    audioViewModel: AudioViewModel,
    settingsViewModel: SettingsViewModel
) {
    MoodyLazyTheme {
        val navController = rememberNavController()
        var showPlayerSheet by remember { mutableStateOf(false) }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "FZ0 Player",
                            fontFamily = FredokaOneFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.secondary
                    )
                )
            },
            bottomBar = {
                Column {
                    // Mini Player above bottom navigation
                    val currentSong by audioViewModel.currentSong.collectAsState()
                    val isPlaying by audioViewModel.isPlaying.collectAsState()
                    val positionMs by audioViewModel.positionMs.collectAsState()
                    val durationMs by audioViewModel.durationMs.collectAsState()

                    MiniPlayer(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        onPlayPauseClick = { audioViewModel.togglePlayPause() },
                        onPreviousClick = { audioViewModel.previous() },
                        onNextClick = { audioViewModel.next() },
                        onMiniPlayerClick = { showPlayerSheet = true }
                    )

                    // Bottom Navigation
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        bottomNavItems.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(audioViewModel = audioViewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(settingsViewModel = settingsViewModel)
                }
                composable(Screen.Search.route) {
                    SearchScreen()
                }
            }
        }

        // Player bottom sheet
        if (showPlayerSheet) {
            PlayerScreen(
                audioViewModel = audioViewModel,
                onDismiss = { showPlayerSheet = false }
            )
        }
    }
}
