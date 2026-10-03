package com.sakatoon.musicplayer.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class MusicThemeTest {
    @Test
    fun unknownSavedThemeFallsBackToDefault() {
        assertEquals(MusicTheme.DEFAULT, MusicTheme.fromStorage("not-a-theme"))
    }

    @Test
    fun themesHaveStableStorageValues() {
        assertEquals(MusicTheme.OCEAN, MusicTheme.fromStorage(MusicTheme.OCEAN.storageValue))
        assertEquals(MusicTheme.FOREST, MusicTheme.fromStorage(MusicTheme.FOREST.storageValue))
        assertEquals(MusicTheme.SUNSET, MusicTheme.fromStorage(MusicTheme.SUNSET.storageValue))
        assertEquals(MusicTheme.LIGHT, MusicTheme.fromStorage(MusicTheme.LIGHT.storageValue))
    }
}
