package com.sakatoon.musicplayer.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.sakatoon.musicplayer.data.db.MusicDao
import com.sakatoon.musicplayer.data.model.Favorite
import com.sakatoon.musicplayer.data.model.Playlist
import com.sakatoon.musicplayer.data.model.PlaylistSong
import com.sakatoon.musicplayer.data.model.Song

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MusicRepository(
    private val musicDao: MusicDao,
    private val context: Context
) {
    private val scanMutex = Mutex()
    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()
    val favoriteSongs: Flow<List<Song>> = musicDao.getFavoriteSongs()
    val favoriteIds: Flow<List<Favorite>> = musicDao.getAllFavoritesIds()



    suspend fun scanMusicFromFolders(folderUris: Set<String>) = withContext(Dispatchers.IO) {
        scanMutex.withLock {
        android.util.Log.d("MusicRepository", "Iniciando escaneo optimizado de ${folderUris.size} carpetas")
        
        val allAudioFiles = mutableListOf<DocumentFile>()
        for (uriString in folderUris) {
            try {
                val folderUri = Uri.parse(uriString)
                val documentFile = DocumentFile.fromTreeUri(context, folderUri)
                if (documentFile == null || !documentFile.isDirectory || !documentFile.canRead()) {
                    throw java.io.IOException("Carpeta sin acceso")
                }
                collectAudioFilesRecursively(documentFile, allAudioFiles)
            } catch (e: Exception) {
                // Do not mistake revoked folder access for an empty library.
                throw java.io.IOException("Vuelve a seleccionar las carpetas de música en Ajustes.", e)
            }
        }

        android.util.Log.d("MusicRepository", "Total archivos encontrados: ${allAudioFiles.size}. Procesando metadatos...")

        val finalSongsList = mutableListOf<Song>()
        val batchSize = 50
        var processedCount = 0

        for (file in allAudioFiles) {
            val uriStr = file.uri.toString()
            // Optimized check: Does it already exist in DB?
            val existingSong = musicDao.getSongByUri(uriStr)
            
            if (existingSong != null) {
                // Reuse existing metadata (FAST)
                finalSongsList.add(existingSong)
            } else {
                // Only extract metadata if not in DB (SLOW)
                val newSong = extractSongMetadata(file)
                if (newSong != null) {
                    finalSongsList.add(newSong)
                }
            }
            
            processedCount++
            if (processedCount % batchSize == 0) {
                android.util.Log.d("MusicRepository", "Procesados $processedCount / ${allAudioFiles.size}")
            }
        }

        // Final Sync with DB
        if (finalSongsList.isNotEmpty()) {
            val allIds = finalSongsList.map { it.id }
            musicDao.deleteSongsNotIn(allIds) // Remove songs that no longer exist in folders
            musicDao.insertSongs(finalSongsList) // Batch update/insert everything else
        } else {
            musicDao.deleteSongsNotIn(emptyList()) // Clear if no songs found
        }

        android.util.Log.d("MusicRepository", "Escaneo optimizado finalizado. Total: ${finalSongsList.size}")
        }
    }

    private fun collectAudioFilesRecursively(parentFolder: DocumentFile, audioFiles: MutableList<DocumentFile>) {
        val files = parentFolder.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                collectAudioFilesRecursively(file, audioFiles)
            } else if (file.isFile) {
                val filename = file.name?.lowercase() ?: ""
                val isAudioMime = file.type?.startsWith("audio/") == true
                val hasAudioExtension = filename.endsWith(".mp3") || 
                                      filename.endsWith(".m4a") || 
                                      filename.endsWith(".wav") || 
                                      filename.endsWith(".flac") || 
                                      filename.endsWith(".ogg") ||
                                      filename.endsWith(".aac")

                if (isAudioMime || hasAudioExtension) {
                    audioFiles.add(file)
                }
            }
        }
    }

    private fun extractSongMetadata(file: DocumentFile): Song? {
        val uri = file.uri
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.name ?: "Unknown"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            val duration = durationStr?.toLongOrNull() ?: 0L
            
            var albumArtUri: String? = null
            val embeddedPicture = retriever.embeddedPicture
            if (embeddedPicture != null) {
                // Save to cache
                try {
                    val artFile = java.io.File(context.cacheDir, "album_art_${uri.hashCode()}.jpg")
                    if (!artFile.exists()) {
                        artFile.writeBytes(embeddedPicture)
                    }
                    albumArtUri = artFile.toURI().toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            

            Song(
                id = uri.toString().hashCode().toLong(), // Stable ID based on URI
                title = title,
                artist = artist,
                album = album,
                uri = uri.toString(),
                duration = duration,
                albumArtUri = albumArtUri
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                // retriever.release() is needed but sometimes throws errors on older devices if not initialized properly
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun createPlaylist(name: String) {
        musicDao.insertPlaylist(Playlist(name = name))
    }
    
    suspend fun deletePlaylist(playlist: Playlist) {
        musicDao.deletePlaylist(playlist)
    }


    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        musicDao.insertPlaylistSong(PlaylistSong(playlistId, songId))
    }
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }
    
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> = musicDao.getSongsForPlaylist(playlistId)

    suspend fun toggleFavorite(song: Song, isFavorite: Boolean) {
        if (isFavorite) {
            musicDao.insertFavorite(Favorite(song.id))
        } else {
            musicDao.removeFavorite(Favorite(song.id))
        }
    }
}
