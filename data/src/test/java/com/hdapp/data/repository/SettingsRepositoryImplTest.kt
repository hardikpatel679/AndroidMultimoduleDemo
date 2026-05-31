package com.hdapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hdapp.domain.model.AppLanguageConfig
import com.hdapp.domain.model.AppThemeConfig
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SettingsRepositoryImplTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        dataStore = mockk(relaxed = true)
        repository = SettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `getTheme returns default when no value saved`() = runTest {
        val preferences = mockk<Preferences>()
        coEvery { preferences[any<Preferences.Key<*>>()] } returns null
        coEvery { dataStore.data } returns flowOf(preferences)
        
        repository.getTheme().test {
            assertThat(awaitItem()).isEqualTo(AppThemeConfig.SYSTEM)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getLanguage returns default when no value saved`() = runTest {
        val preferences = mockk<Preferences>()
        coEvery { preferences[any<Preferences.Key<*>>()] } returns null
        coEvery { dataStore.data } returns flowOf(preferences)

        repository.getLanguage().test {
            assertThat(awaitItem()).isEqualTo(AppLanguageConfig.ENGLISH)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
