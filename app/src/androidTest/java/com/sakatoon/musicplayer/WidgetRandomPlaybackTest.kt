package com.sakatoon.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.os.SystemClock
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.util.concurrent.ListenableFuture
import com.sakatoon.musicplayer.service.MusicService
import com.sakatoon.musicplayer.widget.WidgetActionReceiver
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class WidgetRandomPlaybackTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun <T> request(block: () -> ListenableFuture<T>): T {
        lateinit var future: ListenableFuture<T>
        instrumentation.runOnMainSync { future = block() }
        return future.get(15, TimeUnit.SECONDS)
    }

    @Test
    fun playWithAnEmptyQueueStartsARandomLibrarySong() {
        val context = instrumentation.targetContext
        val browser = request {
            MediaBrowser.Builder(
                context,
                SessionToken(context, ComponentName(context, MusicService::class.java))
            ).buildAsync()
        }

        try {
            val librarySongs = request { browser.getChildren("all_songs", 0, Int.MAX_VALUE, null) }.value.orEmpty()
            assumeTrue("La prueba necesita canciones agregadas", librarySongs.isNotEmpty())
            instrumentation.runOnMainSync {
                browser.pause()
                browser.clearMediaItems()
            }

            context.sendBroadcast(
                Intent(context, WidgetActionReceiver::class.java)
                    .setAction(WidgetActionReceiver.ACTION_PLAY_PAUSE)
            )

            var started = false
            for (attempt in 0 until 50) {
                instrumentation.runOnMainSync {
                    started = browser.isPlaying && browser.currentMediaItem != null
                }
                if (started) break
                SystemClock.sleep(200)
            }
            assertTrue("Play del widget no inició una canción", started)
        } finally {
            instrumentation.runOnMainSync {
                browser.pause()
                browser.release()
            }
        }
    }
}
