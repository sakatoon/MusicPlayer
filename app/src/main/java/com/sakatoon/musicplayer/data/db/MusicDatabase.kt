package com.sakatoon.musicplayer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sakatoon.musicplayer.data.model.Favorite
import com.sakatoon.musicplayer.data.model.Playlist
import com.sakatoon.musicplayer.data.model.PlaylistSong
import com.sakatoon.musicplayer.data.model.Song

@Database(
    entities = [Song::class, Playlist::class, PlaylistSong::class, Favorite::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}
