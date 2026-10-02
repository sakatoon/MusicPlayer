package com.sakatoon.musicplayer

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import com.sakatoon.musicplayer.widget.WidgetActionReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicWidgetsTest {
    @Test
    fun widgetPlaybackActionDoesNotCrashTheApplication() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext

        context.sendBroadcast(
            Intent(context, WidgetActionReceiver::class.java)
                .setAction(WidgetActionReceiver.ACTION_NEXT)
        )
        instrumentation.waitForIdleSync()
        SystemClock.sleep(500)

        assertTrue(true)
    }

    @Test
    fun threeUsableWidgetsArePublished() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val providers = AppWidgetManager.getInstance(context).installedProviders
            .filter { it.provider.packageName == context.packageName }

        assertEquals(3, providers.size)
        assertTrue(providers.all { it.initialLayout != 0 && it.minWidth > 0 && it.minHeight > 0 })
    }
}
