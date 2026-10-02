package com.sakatoon.musicplayer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.sakatoon.musicplayer.MainActivity
import com.sakatoon.musicplayer.R
import com.sakatoon.musicplayer.service.MusicService
import com.sakatoon.musicplayer.ui.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlin.random.Random

internal enum class PlayButtonAction { PAUSE, RESUME, PLAY_RANDOM }

internal fun playButtonAction(isPlaying: Boolean, hasCurrentSong: Boolean): PlayButtonAction = when {
    isPlaying -> PlayButtonAction.PAUSE
    hasCurrentSong -> PlayButtonAction.RESUME
    else -> PlayButtonAction.PLAY_RANDOM
}

internal fun randomSongIndex(
    itemCount: Int,
    currentIndex: Int,
    nextInt: (Int) -> Int = Random::nextInt
): Int? {
    if (itemCount <= 0) return null
    if (itemCount == 1) return 0
    if (currentIndex !in 0 until itemCount) return nextInt(itemCount)
    val candidate = nextInt(itemCount - 1)
    return if (candidate >= currentIndex) candidate + 1 else candidate
}

class CompactPlayerWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, MusicWidgetUpdater.compactViews(context, null)) }
        WidgetActionReceiver.refresh(context)
    }
}

class NowPlayingWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, MusicWidgetUpdater.largeViews(context, null)) }
        WidgetActionReceiver.refresh(context)
    }
}

class MusicShortcutsWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> manager.updateAppWidget(id, MusicWidgetUpdater.shortcutViews(context)) }
    }
}

class WidgetActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        val future = try {
            MediaBrowser.Builder(
                appContext,
                SessionToken(appContext, ComponentName(appContext, MusicService::class.java))
            ).buildAsync()
        } catch (error: RuntimeException) {
            Log.e(TAG, "No se pudo conectar el widget al reproductor", error)
            pendingResult.finish()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            var browser: MediaBrowser? = null
            try {
                browser = future.await()
                when (intent.action) {
                    ACTION_PLAY_PAUSE -> if (browser.isPlaying) browser.pause() else playRandomSong(browser)
                    ACTION_PREVIOUS -> browser.seekToPrevious()
                    ACTION_NEXT -> browser.seekToNext()
                }
                MusicWidgetUpdater.updatePlaybackWidgets(appContext, browser)
            } catch (error: Exception) {
                Log.e(TAG, "No se pudo ejecutar la acción del widget", error)
            } finally {
                browser?.release() ?: future.cancel(true)
                pendingResult.finish()
            }
        }
    }

    private suspend fun playRandomSong(browser: MediaBrowser) {
        val libraryItems = browser.getChildren(ALL_SONGS_ID, 0, Int.MAX_VALUE, null).await().value.orEmpty()
        if (libraryItems.isNotEmpty()) {
            val currentMediaId = browser.currentMediaItem?.mediaId
            val currentIndex = libraryItems.indexOfFirst { it.mediaId == currentMediaId }
            val selectedIndex = randomSongIndex(libraryItems.size, currentIndex) ?: return
            browser.setMediaItems(libraryItems, selectedIndex, 0L)
        } else {
            val selectedIndex = randomSongIndex(browser.mediaItemCount, browser.currentMediaItemIndex) ?: return
            browser.seekTo(selectedIndex, 0L)
        }
        browser.prepare()
        browser.play()
    }

    companion object {
        private const val TAG = "WidgetActionReceiver"
        private const val ALL_SONGS_ID = "all_songs"
        const val ACTION_PLAY_PAUSE = "com.sakatoon.musicplayer.widget.PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.sakatoon.musicplayer.widget.PREVIOUS"
        const val ACTION_NEXT = "com.sakatoon.musicplayer.widget.NEXT"
        private const val ACTION_REFRESH = "com.sakatoon.musicplayer.widget.REFRESH"
        fun refresh(context: Context) {
            context.sendBroadcast(Intent(context, WidgetActionReceiver::class.java).setAction(ACTION_REFRESH))
        }
    }
}

object MusicWidgetUpdater {
    fun updatePlaybackWidgets(context: Context, player: Player) {
        val manager = AppWidgetManager.getInstance(context)
        manager.updateAppWidget(ComponentName(context, CompactPlayerWidget::class.java), compactViews(context, player))
        manager.updateAppWidget(ComponentName(context, NowPlayingWidget::class.java), largeViews(context, player))
    }

    fun compactViews(context: Context, player: Player?) = RemoteViews(context.packageName, R.layout.widget_compact_player).apply {
        setTextViewText(R.id.widget_title, player?.mediaMetadata?.title ?: "Nada en reproducción")
        setTextViewText(R.id.widget_artist, player?.mediaMetadata?.artist ?: "SakaToOn MusicPlayer")
        setImageViewResource(R.id.widget_play_pause, if (player?.isPlaying == true) R.drawable.ic_widget_pause else R.drawable.ic_widget_play)
        wirePlaybackActions(context, this)
        setOnClickPendingIntent(R.id.widget_root, openApp(context, Screen.Player.route, 10))
    }

    fun largeViews(context: Context, player: Player?) = RemoteViews(context.packageName, R.layout.widget_now_playing).apply {
        setTextViewText(R.id.widget_title, player?.mediaMetadata?.title ?: "Nada en reproducción")
        setTextViewText(R.id.widget_artist, player?.mediaMetadata?.artist ?: "Elige una canción en MusicPlayer")
        setImageViewResource(R.id.widget_play_pause, if (player?.isPlaying == true) R.drawable.ic_widget_pause else R.drawable.ic_widget_play)
        val duration = player?.duration?.coerceAtLeast(0) ?: 0
        val position = player?.currentPosition?.coerceIn(0, duration) ?: 0
        setProgressBar(R.id.widget_progress, duration.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), position.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), duration == 0L)
        val artwork = player?.mediaMetadata?.artworkUri?.let { loadArtwork(context, it) }
        if (artwork != null) setImageViewBitmap(R.id.widget_artwork, artwork) else setImageViewResource(R.id.widget_artwork, R.drawable.widget_music_art)
        wirePlaybackActions(context, this)
        setOnClickPendingIntent(R.id.widget_root, openApp(context, Screen.Player.route, 20))
    }

    fun shortcutViews(context: Context) = RemoteViews(context.packageName, R.layout.widget_music_shortcuts).apply {
        setOnClickPendingIntent(R.id.shortcut_library, openApp(context, Screen.Home.route, 30))
        setOnClickPendingIntent(R.id.shortcut_favorites, openApp(context, Screen.Favorites.route, 31))
        setOnClickPendingIntent(R.id.shortcut_playlists, openApp(context, Screen.Playlists.route, 32))
    }

    private fun wirePlaybackActions(context: Context, views: RemoteViews) {
        views.setOnClickPendingIntent(R.id.widget_previous, action(context, WidgetActionReceiver.ACTION_PREVIOUS, 1))
        views.setOnClickPendingIntent(R.id.widget_play_pause, action(context, WidgetActionReceiver.ACTION_PLAY_PAUSE, 2))
        views.setOnClickPendingIntent(R.id.widget_next, action(context, WidgetActionReceiver.ACTION_NEXT, 3))
    }

    private fun action(context: Context, action: String, requestCode: Int) = PendingIntent.getBroadcast(
        context, requestCode, Intent(context, WidgetActionReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun openApp(context: Context, destination: String, requestCode: Int) = PendingIntent.getActivity(
        context, requestCode,
        Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_DESTINATION, destination)
            .setData(Uri.parse("sakatoon://musicplayer/$destination")),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun loadArtwork(context: Context, uri: Uri) = try {
        context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
    } catch (_: Exception) { null }
}
