
package com.sakatoon.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sakatoon.musicplayer.ui.screens.*
import com.sakatoon.musicplayer.ui.viewmodel.MusicViewModel

@Composable
fun MusicNavHost(
    navController: NavHostController,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onSongClick = { song ->
                    viewModel.playSong(song)
                    navController.navigate(Screen.Player.route)
                },
                onPlayerClick = {
                    navController.navigate(Screen.Player.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Player.route) {
            PlayerScreen(viewModel = viewModel)
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                viewModel = viewModel,
                onPlaylistClick = { playlist ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlist.playlistId, playlist.name))
                }
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(viewModel = viewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,

                onFolderSelected = {
                    // Navigate back to Home/Library and clear stack up to Home to avoid back loops
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("playlistId") { type = androidx.navigation.NavType.LongType },
                androidx.navigation.navArgument("playlistName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            val playlistName = backStackEntry.arguments?.getString("playlistName") ?: "Playlist"
            
            PlaylistDetailScreen(
                playlistId = playlistId,
                playlistName = playlistName,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSongClick = { song ->
                    viewModel.playSong(song)
                    navController.navigate(Screen.Player.route)
                },
                onPlayerClick = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }


    }
}
