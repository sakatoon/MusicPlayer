package com.sakatoon.musicplayer.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sakatoon.musicplayer.data.model.Favorite
import com.sakatoon.musicplayer.data.model.Playlist
import com.sakatoon.musicplayer.data.model.PlaylistSong
import com.sakatoon.musicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    // Songs (Cache)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE uri = :uri LIMIT 1")
    suspend fun getSongByUri(uri: String): Song?

    @Query("DELETE FROM songs WHERE id NOT IN (:ids)")
    suspend fun deleteSongsNotIn(ids: List<Long>)

    // Playlists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    // Playlist Songs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSong)

    @Query("SELECT s.* FROM songs s INNER JOIN playlist_songs ps ON s.id = ps.songId WHERE ps.playlistId = :playlistId")
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    // Favorites
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun removeFavorite(favorite: Favorite)

    @Query("SELECT * FROM favorites")
    fun getAllFavoritesIds(): Flow<List<Favorite>>

    @Query("SELECT s.* FROM songs s INNER JOIN favorites f ON s.id = f.songId")
    fun getFavoriteSongs(): Flow<List<Song>>
}
