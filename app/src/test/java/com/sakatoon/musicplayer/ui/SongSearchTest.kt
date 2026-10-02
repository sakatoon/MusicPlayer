package com.sakatoon.musicplayer.ui

import com.sakatoon.musicplayer.data.model.Song
import com.sakatoon.musicplayer.ui.screens.filterSongs
import org.junit.Assert.assertEquals
import org.junit.Test

class SongSearchTest {
    private val songs = listOf(
        Song(1, "Luz de Luna", "SakaToOn", "Noches", "file:///luna.mp3", 1000),
        Song(2, "Camino", "Los Viajeros", "Rutas", "file:///camino.mp3", 1000),
        Song(3, "Corazón", "SakaToOn", "Acústico", "file:///corazon.mp3", 1000)
    )

    @Test
    fun searchesTitleArtistAndAlbumIgnoringCaseAndAccents() {
        assertEquals(listOf(1L), filterSongs(songs, "luna").map { it.id })
        assertEquals(listOf(1L, 3L), filterSongs(songs, "sakatoon").map { it.id })
        assertEquals(listOf(3L), filterSongs(songs, "corazon").map { it.id })
    }

    @Test
    fun blankSearchShowsTheWholeLibrary() {
        assertEquals(songs, filterSongs(songs, "  "))
    }
}
