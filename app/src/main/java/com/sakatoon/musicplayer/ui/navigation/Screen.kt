package com.sakatoon.musicplayer.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Player : Screen("player")
    object Playlists : Screen("playlists")

    object Favorites : Screen("favorites")
    object Equalizer : Screen("equalizer")
    object Settings : Screen("settings")
    object Developer : Screen("developer")
    object PlaylistDetail : Screen("playlist_detail/{playlistId}/{playlistName}") {

        fun createRoute(playlistId: Long, playlistName: String) = "playlist_detail/$playlistId/${android.net.Uri.encode(playlistName)}"
    }


}
