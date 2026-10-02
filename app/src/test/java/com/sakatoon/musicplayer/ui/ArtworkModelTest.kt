package com.sakatoon.musicplayer.ui

import com.sakatoon.musicplayer.ui.components.resolveArtworkModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtworkModelTest {
    @Test fun missingOrBlankArtworkUsesFallbackResource() {
        assertEquals(42, resolveArtworkModel(null, 42))
        assertEquals(42, resolveArtworkModel("   ", 42))
    }

    @Test fun validArtworkKeepsItsUri() {
        assertEquals("file:///cover.jpg", resolveArtworkModel("file:///cover.jpg", 42))
    }
}
