package com.sakatoon.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {
    private val FOLDER_URIS_KEY = androidx.datastore.preferences.core.stringSetPreferencesKey("music_folder_uris")

    val musicFolderUris: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FOLDER_URIS_KEY] ?: emptySet()
        }

    suspend fun addMusicFolderUri(uri: String) {
        context.dataStore.edit { preferences ->
            val currentUris = preferences[FOLDER_URIS_KEY] ?: emptySet()
            preferences[FOLDER_URIS_KEY] = currentUris + uri
        }
    }

    suspend fun removeMusicFolderUri(uri: String) {
        context.dataStore.edit { preferences ->
            val currentUris = preferences[FOLDER_URIS_KEY] ?: emptySet()
            preferences[FOLDER_URIS_KEY] = currentUris - uri
        }
    }
}
