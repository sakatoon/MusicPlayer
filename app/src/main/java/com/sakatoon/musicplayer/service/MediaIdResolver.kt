package com.sakatoon.musicplayer.service

import com.sakatoon.musicplayer.data.model.Song

internal object MediaIdResolver {
    fun search(songs: List<Song>, query: String): List<Song> {
        val term = query.trim()
        return songs.filter {
            it.title.contains(term, ignoreCase = true) ||
                it.artist.contains(term, ignoreCase = true) ||
                it.album.contains(term, ignoreCase = true)
        }
    }

    fun findSong(songs: List<Song>, mediaId: String?): Song? {
        if (mediaId.isNullOrBlank()) return null
        val numericId = mediaId.toLongOrNull()
        return songs.firstOrNull { song -> song.id == numericId || song.uri == mediaId }
    }
}
