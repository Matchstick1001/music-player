package com.example.fzo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
import com.example.fzo.ui.components.AnimatedAppTitle

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
                    title = { AnimatedAppTitle() },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                Column {
                    // Mini Player above bottom navigation
                    val currentSong by audioViewModel.currentSong.collectAsState()
                    val isPlaying by audioViewModel.isPlaying.collectAsState()

                    MiniPlayer(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onPlayPauseClick = { audioViewModel.togglePlayPause() },
                        onMiniPlayerClick = { showPlayerSheet = true }
                    )

                    // Bottom Navigation
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
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
