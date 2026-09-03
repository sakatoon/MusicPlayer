package com.sakatoon.musicplayer


import android.app.Application
import android.content.Context
import androidx.room.Room
import com.sakatoon.musicplayer.data.db.MusicDatabase
import com.sakatoon.musicplayer.data.repository.MusicRepository
import com.sakatoon.musicplayer.data.repository.UserPreferencesRepository

class MusicApplication : Application() {
    
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(private val context: Context) {
    val database by lazy {
        Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "music_database"
        ).build()
    }

    val musicRepository by lazy {
        MusicRepository(database.musicDao(), context)
    }

    val userPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }
}
