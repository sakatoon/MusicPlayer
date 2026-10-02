package com.sakatoon.musicplayer.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesRepositoryTest {
    @Test
    fun shufflePreferenceIsRestoredByANewRepositoryInstance() = runBlocking {
        val file = File.createTempFile("music-player-settings", ".preferences_pb").apply { delete() }
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }

        try {
            val firstRepository = UserPreferencesRepository(dataStore)
            assertFalse(firstRepository.shuffleEnabled.first())

            firstRepository.setShuffleEnabled(true)

            val restoredRepository = UserPreferencesRepository(dataStore)
            assertTrue(restoredRepository.shuffleEnabled.first())
        } finally {
            scope.cancel()
            file.delete()
        }
    }
}
