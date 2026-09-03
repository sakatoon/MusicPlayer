
package com.sakatoon.musicplayer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sakatoon.musicplayer.ui.navigation.Screen

@Composable
fun MusicNavigationRail(navController: NavController) {
    NavigationRail {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationRailItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = currentRoute == Screen.Home.route,

            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) {
                         // Use startDestinationId instead of explicit route and disable saveState to force reset
                         // inclusive = true // inclusive true pops the start destination too which we might not want if we want to reset TO it
                    }
                    launchSingleTop = true
                }
            }
        )

        NavigationRailItem(
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Listas") },
            label = { Text("Listas") },
            selected = currentRoute == Screen.Playlists.route,
            onClick = {
                navController.navigate(Screen.Playlists.route) {
                    popUpTo(Screen.Home.route)
                }
            }
        )
        NavigationRailItem(
             icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
            label = { Text("Favoritos") },
            selected = currentRoute == Screen.Favorites.route,
            onClick = {
                navController.navigate(Screen.Favorites.route) {
                    popUpTo(Screen.Home.route)
                }
            }
        )
        NavigationRailItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
            label = { Text("Ajustes") },
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                navController.navigate(Screen.Settings.route) {
                    popUpTo(Screen.Home.route)
                }
            }
        )

    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun MusicNavigationRailPreview() {
    MusicNavigationRail(navController = androidx.navigation.compose.rememberNavController())
}
