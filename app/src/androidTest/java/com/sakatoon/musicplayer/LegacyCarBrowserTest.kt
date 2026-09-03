package com.sakatoon.musicplayer

import android.content.ComponentName
import android.content.pm.PackageManager
import android.media.browse.MediaBrowser
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.util.concurrent.SettableFuture
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

/** Also covers the legacy browser protocol used by older car hosts. */
class LegacyCarBrowserTest {
    @Test
    fun legacyHostCanBrowseSongs() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val connected = SettableFuture.create<Boolean>()
        lateinit var browser: MediaBrowser
        instrumentation.runOnMainSync {
            browser = MediaBrowser(context,
                ComponentName(context, "com.sakatoon.musicplayer.service.MusicService"),
                object : MediaBrowser.ConnectionCallback() {
                    override fun onConnected() { connected.set(true) }
                    override fun onConnectionFailed() { connected.set(false) }
                }, null)
            browser.connect()
        }
        try {
            assertTrue(connected.get(15, TimeUnit.SECONDS))
            val result = SettableFuture.create<List<MediaBrowser.MediaItem>>()
            instrumentation.runOnMainSync {
                browser.subscribe("all_songs", object : MediaBrowser.SubscriptionCallback() {
                    override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowser.MediaItem>) {
                        result.set(children)
                    }
                    override fun onError(parentId: String) {
                        result.setException(AssertionError("El catálogo no está disponible"))
                    }
                })
            }
            result.get(15, TimeUnit.SECONDS).forEach { assertTrue(it.isPlayable) }
        } finally {
            instrumentation.runOnMainSync { browser.disconnect() }
        }
    }

    @Test
    fun carServiceDeclaresResolvableLauncherAndAttributionIcons() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.packageManager
        val service = manager.getServiceInfo(ComponentName(context,
            "com.sakatoon.musicplayer.service.MusicService"), 0)
        assertEquals(R.mipmap.ic_launcher, service.icon)
        assertNotNull(service.loadIcon(manager))
        val app = manager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        assertEquals(R.drawable.ic_music_status, app.metaData.getInt("androidx.car.app.TintableAttributionIcon"))
        assertNotNull(context.getDrawable(app.metaData.getInt("androidx.car.app.TintableAttributionIcon")))
    }
}
