package com.sakatoon.musicplayer.service

import com.sakatoon.musicplayer.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaIdResolverTest {
    private val song = Song(
        id = 42,
        title = "Canción",
        artist = "Artista",
        album = "Álbum",
        uri = "content://music/audio/42",
        duration = 180_000
    )

    @Test
    fun `finds a song by the numeric id used by the phone player`() {
        assertEquals(song, MediaIdResolver.findSong(listOf(song), "42"))
    }

    @Test
    fun `finds a song by the content uri exposed to Android Auto`() {
        assertEquals(song, MediaIdResolver.findSong(listOf(song), song.uri))
    }

    @Test
    fun `returns null for an unknown media id`() {
        assertNull(MediaIdResolver.findSong(listOf(song), "unknown"))
    }

    @Test
    fun `search matches title artist and album without case sensitivity`() {
        for (query in listOf("CANCIÓN", " artista ", "ÁLBUM")) {
            assertEquals(listOf(song), MediaIdResolver.search(listOf(song), query))
        }
        assertEquals(emptyList<Song>(), MediaIdResolver.search(listOf(song), "missing"))
    }

    @Test
    fun `empty voice query returns the available library`() {
        assertEquals(listOf(song), MediaIdResolver.search(listOf(song), " "))
    }
}
