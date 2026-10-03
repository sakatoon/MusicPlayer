package com.sakatoon.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val settings: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.dataStore)

    private val folderUrisKey = androidx.datastore.preferences.core.stringSetPreferencesKey("music_folder_uris")
    private val shuffleEnabledKey = booleanPreferencesKey("shuffle_enabled")
    private val themeKey = stringPreferencesKey("music_theme")

    val musicFolderUris: Flow<Set<String>> = settings.data
        .map { preferences ->
            preferences[folderUrisKey] ?: emptySet()
        }
        .distinctUntilChanged()

    val shuffleEnabled: Flow<Boolean> = settings.data
        .map { preferences -> preferences[shuffleEnabledKey] ?: false }
        .distinctUntilChanged()

    val theme: Flow<String?> = settings.data
        .map { preferences -> preferences[themeKey] }
        .distinctUntilChanged()

    suspend fun addMusicFolderUri(uri: String) {
        settings.edit { preferences ->
            val currentUris = preferences[folderUrisKey] ?: emptySet()
            preferences[folderUrisKey] = currentUris + uri
        }
    }

    suspend fun removeMusicFolderUri(uri: String) {
        settings.edit { preferences ->
            val currentUris = preferences[folderUrisKey] ?: emptySet()
            preferences[folderUrisKey] = currentUris - uri
        }
    }

    suspend fun setShuffleEnabled(enabled: Boolean) {
        settings.edit { preferences -> preferences[shuffleEnabledKey] = enabled }
    }

    suspend fun setTheme(value: String) {
        settings.edit { preferences -> preferences[themeKey] = value }
    }
}
