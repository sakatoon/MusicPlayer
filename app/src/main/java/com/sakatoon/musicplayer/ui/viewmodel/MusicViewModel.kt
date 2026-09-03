package com.sakatoon.musicplayer.ui.viewmodel


import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

import com.sakatoon.musicplayer.MusicApplication
import com.sakatoon.musicplayer.data.model.Playlist
import com.sakatoon.musicplayer.data.model.Song
import com.sakatoon.musicplayer.data.repository.MusicRepository
import com.sakatoon.musicplayer.data.repository.UserPreferencesRepository

import com.sakatoon.musicplayer.service.MusicService
import com.sakatoon.musicplayer.service.MediaIdResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class MusicUiState(
    val songs: List<Song> = emptyList(),
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,


    val folderUris: Set<String> = emptySet(),
    val folderNames: List<String> = emptyList(),
    val isScanning: Boolean = false
)

class MusicViewModel(

    private val musicRepository: MusicRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var player: Player? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        mediaControllerFuture?.addListener({
            try {
                player = mediaControllerFuture?.get()
                setupPlayerListener()
                updateCurrentSong(player?.currentMediaItem)
                _uiState.value = _uiState.value.copy(isPlaying = player?.isPlaying == true)
                viewModelScope.launch {
                    while (true) {
                        updateProgress()
                        delay(500)
                    }
                }

            } catch (e: Exception) {
                // Errors logged, avoiding breadcrumbs on startup
                Log.e("MusicViewModel", "Failed to connect to MusicService", e)
            }
        }, MoreExecutors.directExecutor())


        viewModelScope.launch {
            userPreferencesRepository.musicFolderUris.collectLatest { uris ->
                val names = uris.map { getFolderName(it) ?: it }
                _uiState.value = _uiState.value.copy(folderUris = uris, folderNames = names)
                scanAllFolders()
            }
        }


        viewModelScope.launch {
            musicRepository.allSongs.collectLatest { songs ->
                _uiState.value = _uiState.value.copy(songs = songs)
                updateCurrentSong(player?.currentMediaItem)
            }
        }

        viewModelScope.launch {
            musicRepository.favoriteIds.collectLatest { favorites ->
                _favoriteIds.value = favorites.map { it.songId }.toSet()
            }
        }
    }

    private fun setupPlayerListener() {
        player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSong(mediaItem)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _uiState.value = _uiState.value.copy(duration = player?.duration ?: 0L)
                }
            }
        })
    }

    private fun updateCurrentSong(mediaItem: MediaItem?) {
        val song = MediaIdResolver.findSong(_uiState.value.songs, mediaItem?.mediaId)
        _uiState.value = _uiState.value.copy(currentSong = song)
    }

    fun playSong(song: Song) {
        val index = _uiState.value.songs.indexOf(song)
        if (index != -1) {
            val mediaItems = _uiState.value.songs.map {
                MediaItem.Builder()
                    .setMediaId(it.id.toString())
                    .setUri(it.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(it.title)
                            .setArtist(it.artist)
                            .setAlbumTitle(it.album)
                            .setArtworkUri(Uri.parse(it.albumArtUri ?: "android.resource://${context.packageName}/drawable/sakatoon_mp"))
                            .build()
                    )
                    .build()
            }
            

            if (player == null) {
                Log.e("MusicViewModel", "Player is null")
                Toast.makeText(context, "El reproductor no está listo. Por favor espera...", Toast.LENGTH_SHORT).show()
                return
            }

            player?.setMediaItems(mediaItems, index, 0)
            player?.prepare()
            player?.play()
        }
    }

    fun togglePlayPause() {
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    fun seekTo(position: Float) {
        val duration = player?.duration ?: 0L
        if (duration <= 0 || !position.isFinite()) return
        val newPosition = (duration * position.coerceIn(0f, 1f)).toLong()
        player?.seekTo(newPosition)
        updateProgress()
    }

    fun skipNext() {
        player?.seekToNext()
    }


    fun skipPrevious() {
        player?.seekToPrevious()
    }

    fun addMusicFolder(uri: Uri) {
        viewModelScope.launch {
            userPreferencesRepository.addMusicFolderUri(uri.toString())
        }
    }

    fun removeMusicFolder(uriString: String) {
        viewModelScope.launch {
            userPreferencesRepository.removeMusicFolderUri(uriString)
        }
    }

    fun scanAllFolders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            try {
                musicRepository.scanMusicFromFolders(_uiState.value.folderUris)
            } catch (e: java.io.IOException) {
                Toast.makeText(context, e.message ?: "No se pudo leer la biblioteca", Toast.LENGTH_LONG).show()
            } finally {
                _uiState.value = _uiState.value.copy(isScanning = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
    }


    // Playlists & Favorites
    
    val allPlaylists: Flow<List<Playlist>> = musicRepository.allPlaylists
    val favoriteSongs: Flow<List<Song>> = musicRepository.favoriteSongs
    
    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            musicRepository.createPlaylist(name)
        }
    }
    
    fun deletePlaylist(playlist: Playlist) {
         viewModelScope.launch {
            musicRepository.deletePlaylist(playlist)
        }
    }



    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, song.id)
        }
    }
    
     fun removeSongFromPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(playlistId, song.id)
        }
    }
    
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return musicRepository.getSongsForPlaylist(playlistId)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFavorite = _favoriteIds.value.contains(song.id)
            musicRepository.toggleFavorite(song, !isFavorite)
        }
    }
    
    fun isFavorite(songId: Long): Boolean {
        return _favoriteIds.value.contains(songId)
    }
    
    fun updateProgress() {
        player?.let {
            val duration = it.duration.coerceAtLeast(0L)
            val current = it.currentPosition.coerceIn(0L, duration)
            _uiState.value = _uiState.value.copy(
                currentPosition = current,
                duration = duration,
                progress = if (duration > 0) current.toFloat() / duration else 0f
            )
        }
    }
    
    private fun getFolderName(uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)?.name
        } catch (e: Exception) {
            uriString // Fallback
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MusicApplication)
                MusicViewModel(
                    musicRepository = application.container.musicRepository,
                    userPreferencesRepository = application.container.userPreferencesRepository,
                    context = application.applicationContext
                )
            }
        }
    }
}
