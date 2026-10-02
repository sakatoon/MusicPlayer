package com.sakatoon.musicplayer.ui

import com.sakatoon.musicplayer.ui.screens.folderSongCountText
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderSongCountTextTest {
    @Test fun showsSingularAndPluralCounts() {
        assertEquals("1 canción encontrada", folderSongCountText(1, false))
        assertEquals("8 canciones encontradas", folderSongCountText(8, false))
    }

    @Test fun showsPendingScanStates() {
        assertEquals("Escaneando…", folderSongCountText(null, true))
        assertEquals("Escaneando…", folderSongCountText(4, true))
        assertEquals("Pendiente de escaneo", folderSongCountText(null, false))
    }
}
