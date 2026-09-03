package com.sakatoon.musicplayer

import android.content.ComponentName
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.util.concurrent.ListenableFuture
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/** Exercises the same library API used by car hosts without modifying the user's library. */
class MediaLibraryTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var browser: MediaBrowser

    private fun <T> request(block: () -> ListenableFuture<T>): T {
        lateinit var future: ListenableFuture<T>
        instrumentation.runOnMainSync { future = block() }
        return future.get(15, TimeUnit.SECONDS)
    }

    @Before
    fun connect() {
        val context = instrumentation.targetContext
        browser = request {
            MediaBrowser.Builder(context, SessionToken(context,
                ComponentName(context, "com.sakatoon.musicplayer.service.MusicService"))).buildAsync()
        }
    }

    @After
    fun disconnect() {
        if (::browser.isInitialized) instrumentation.runOnMainSync { browser.release() }
    }

    @Test
    fun rootCanBeRetrievedIndividually() {
        val root = request { browser.getLibraryRoot(null) }.value!!
        assertTrue(root.mediaMetadata.isBrowsable == true)
        assertFalse(root.mediaMetadata.isPlayable == true)
        val item = request { browser.getItem(root.mediaId) }
        assertEquals(SessionResult.RESULT_SUCCESS, item.resultCode)
        assertEquals(root.mediaId, item.value!!.mediaId)
    }

    @Test
    fun rootMenuHonorsPagination() {
        val first = request { browser.getChildren("root", 0, 1, null) }.value!!
        val second = request { browser.getChildren("root", 1, 1, null) }.value!!
        assertEquals(listOf("all_songs"), first.map { it.mediaId })
        assertEquals(listOf("favorites"), second.map { it.mediaId })
    }

    @Test
    fun addedSongsHavePlayableMetadataAndCanBeLookedUp() {
        val songs = request { browser.getChildren("all_songs", 0, 10, null) }.value!!
        for (song in songs) {
            assertEquals(true, song.mediaMetadata.isPlayable)
            assertEquals(false, song.mediaMetadata.isBrowsable)
            assertNotNull(song.mediaMetadata.title)
            val item = request { browser.getItem(song.mediaId) }
            assertEquals(SessionResult.RESULT_SUCCESS, item.resultCode)
            assertEquals(song.mediaId, item.value!!.mediaId)
        }
    }
}
