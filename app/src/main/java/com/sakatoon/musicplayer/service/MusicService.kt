package com.sakatoon.musicplayer.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn


import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import kotlinx.coroutines.launch




import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition


import com.sakatoon.musicplayer.MainActivity
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.SupervisorJob
import com.sakatoon.musicplayer.MusicApplication

@OptIn(UnstableApi::class)
class MusicService : MediaLibraryService() {

    private var mediaSession: MediaLibrarySession? = null
    private lateinit var player: ExoPlayer


    @OptIn(UnstableApi::class) 
    override fun onCreate() {
        super.onCreate()
        
        player = androidx.media3.exoplayer.ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaLibrarySession.Builder(this, player, LibrarySessionCallback())
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
        val repository = (application as MusicApplication).container.musicRepository
        scope.launch {
            repository.allSongs.collect { songs ->
                mediaSession?.notifyChildrenChanged("all_songs", songs.size, null)
            }
        }
        scope.launch {
            repository.favoriteSongs.collect { songs ->
                mediaSession?.notifyChildrenChanged("favorites", songs.size, null)
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession
    }




    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
                .add(SessionCommand(SessionCommand.COMMAND_CODE_LIBRARY_GET_LIBRARY_ROOT))
                .build()
            
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands,
                connectionResult.availablePlayerCommands
            )
        }


        
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val extras = Bundle().apply {
                putInt("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT", 1)
                putInt("android.media.browse.CONTENT_STYLE_PLAYABLE_HINT", 1)
            }
            val rootItem = MediaItem.Builder()
                .setMediaId("root")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                        .setTitle("Biblioteca de Música")
                        .setExtras(extras)
                        .build()
                )
                .build()
            val result: LibraryResult<MediaItem> = LibraryResult.ofItem(rootItem,
                LibraryParams.Builder().setExtras(extras).build())
            return Futures.immediateFuture(result)
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> = scope.future {
            val item = when (mediaId) {
                "root" -> createBrowsableItem("root", "Biblioteca de Música", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                "all_songs" -> createBrowsableItem("all_songs", "Todas las canciones", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                "favorites" -> createBrowsableItem("favorites", "Favoritos", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                else -> MediaIdResolver.findSong(
                    (application as MusicApplication).container.musicRepository.allSongs.first(), mediaId
                )?.let { createPlayableItem(it) }
            }
            if (item == null) LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            else LibraryResult.ofItem(item, null)
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> = scope.future {
            val songs = (application as MusicApplication).container.musicRepository.allSongs.first()
            session.notifySearchResultChanged(browser, query, MediaIdResolver.search(songs, query).size, params)
            LibraryResult.ofVoid()
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = scope.future {
            if (page < 0 || pageSize < 1) return@future LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            val songs = (application as MusicApplication).container.musicRepository.allSongs.first()
            LibraryResult.ofItemList(pageItems(MediaIdResolver.search(songs, query), page, pageSize)
                .map { createPlayableItem(it) }, params)
        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> = scope.future {
            val repository = (application as MusicApplication).container.musicRepository
            val count = when (parentId) {
                "root" -> 2
                "all_songs" -> repository.allSongs.first().size
                "favorites" -> repository.favoriteSongs.first().size
                else -> return@future LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            }
            session.notifyChildrenChanged(browser, parentId, count, params)
            LibraryResult.ofVoid()
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val app = application as MusicApplication
            val repository = app.container.musicRepository
            if (page < 0 || pageSize < 1) {
                return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
            }
            
            return when (parentId) {
                "root" -> {
                    val items = ImmutableList.of(
                        createBrowsableItem("all_songs", "Todas las Canciones", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED),
                        createBrowsableItem("favorites", "Favoritos", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                    )
                    Futures.immediateFuture(LibraryResult.ofItemList(pageItems(items, page, pageSize), params))
                }
                "all_songs" -> {
                    scope.future {
                        val songs = repository.allSongs.first()
                        val items = songs.map { song -> createPlayableItem(song) }
                        LibraryResult.ofItemList(pageItems(items, page, pageSize), params)
                    }
                }
                "favorites" -> {
                    scope.future {
                        val favorites = repository.favoriteSongs.first()
                        val items = favorites.map { song -> createPlayableItem(song) }
                        LibraryResult.ofItemList(pageItems(items, page, pageSize), params)
                    }
                }
                else -> Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
            }
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            return scope.future {
                val songs = (application as MusicApplication).container.musicRepository.allSongs.first()
                mediaItems.map { item ->
                    val song = requireNotNull(MediaIdResolver.findSong(songs, item.mediaId)) {
                        "La canción no está en la biblioteca"
                    }
                    createPlayableItem(song)
                }.toMutableList()
            }
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaItemsWithStartPosition> {
            val app = application as MusicApplication
            val repository = app.container.musicRepository
            
            return scope.future {
                val songs = repository.allSongs.first()
                val query = mediaItems.singleOrNull()?.requestMetadata?.searchQuery
                if (query != null) {
                    val matches = MediaIdResolver.search(songs, query)
                    require(matches.isNotEmpty()) { "No hay canciones que coincidan" }
                    return@future MediaItemsWithStartPosition(matches.map { createPlayableItem(it) }, 0, startPositionMs)
                }
                val updatedItems = mediaItems.map { item ->
                    val song = requireNotNull(MediaIdResolver.findSong(songs, item.mediaId)) {
                        "La canción no está en la biblioteca"
                    }
                    createPlayableItem(song)
                }.toMutableList()
                // Car hosts send only an ID: retain the library as the next/previous queue.
                if (mediaItems.size == 1 && mediaItems[0].localConfiguration == null) {
                    val selected = MediaIdResolver.findSong(songs, mediaItems[0].mediaId)!!
                    MediaItemsWithStartPosition(songs.map { createPlayableItem(it) },
                        songs.indexOf(selected), startPositionMs)
                } else {
                    MediaItemsWithStartPosition(updatedItems, startIndex, startPositionMs)
                }
            }
        }
    }

    private fun <T> pageItems(items: List<T>, page: Int, pageSize: Int): List<T> {
        val start = (page.toLong() * pageSize).coerceAtMost(items.size.toLong()).toInt()
        val end = (start.toLong() + pageSize).coerceAtMost(items.size.toLong()).toInt()
        return items.subList(start, end)
    }





    private fun createBrowsableItem(id: String, title: String, mediaType: @MediaMetadata.MediaType Int): MediaItem {
        val extras = Bundle().apply {
            putInt("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT", 1)
        }
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setMediaType(mediaType)
                    .setExtras(extras)
                    .build()
            )
            .build()
    }

    private fun createPlayableItem(song: com.sakatoon.musicplayer.data.model.Song): MediaItem {
        val extras = Bundle().apply {
            putInt("android.media.browse.CONTENT_STYLE_PLAYABLE_HINT", 1)
        }
        val artworkUri = song.albumArtUri?.let { android.net.Uri.parse(it) } ?: android.net.Uri.parse("android.resource://${packageName}/drawable/sakatoon_mp")
        return MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(artworkUri)
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .setExtras(extras)
                    .build()
            )
            .build()
    }
    

    // Use a service scope for coroutines
    private val serviceJob = SupervisorJob()
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + serviceJob)


    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceJob.cancel()
        super.onDestroy()
    }
}
