package com.sakatoon.musicplayer.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val uri: String, // String representation of Uri
    val duration: Long,
    val albumArtUri: String? = null
)
