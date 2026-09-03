package com.sakatoon.musicplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSong(
    val playlistId: Long,
    val songId: Long
)
