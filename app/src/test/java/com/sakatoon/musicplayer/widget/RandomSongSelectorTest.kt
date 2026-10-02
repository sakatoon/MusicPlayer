package com.sakatoon.musicplayer.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RandomSongSelectorTest {
    @Test
    fun playWithoutACurrentSongRequiresRandomSelection() {
        assertEquals(PlayButtonAction.PLAY_RANDOM, playButtonAction(isPlaying = false, hasCurrentSong = false))
    }

    @Test
    fun playWithAPausedSongResumesIt() {
        assertEquals(PlayButtonAction.RESUME, playButtonAction(isPlaying = false, hasCurrentSong = true))
    }

    @Test
    fun playWhilePlayingPausesIt() {
        assertEquals(PlayButtonAction.PAUSE, playButtonAction(isPlaying = true, hasCurrentSong = true))
    }

    @Test
    fun emptyLibraryHasNoRandomSelection() {
        assertNull(randomSongIndex(itemCount = 0, currentIndex = -1) { 0 })
    }

    @Test
    fun randomSelectionAvoidsRepeatingTheCurrentSong() {
        assertEquals(2, randomSongIndex(itemCount = 4, currentIndex = 1) { 1 })
    }

    @Test
    fun singleSongLibrarySelectsItsOnlySong() {
        assertEquals(0, randomSongIndex(itemCount = 1, currentIndex = 0) { 0 })
    }
}
