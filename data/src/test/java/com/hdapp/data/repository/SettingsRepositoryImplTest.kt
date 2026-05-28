package com.hdapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
        coEvery { dataStore.data } returns flowOf(mockk(relaxed = true))
        
        repository.getTheme().test {
            assertThat(awaitItem()).isEqualTo(AppThemeConfig.SYSTEM)
        }
    }

    @Test
    fun `getLanguage returns default when no value saved`() = runTest {
        coEvery { dataStore.data } returns flowOf(mockk(relaxed = true))

        repository.getLanguage().test {
            assertThat(awaitItem()).isEqualTo(AppLanguageConfig.ENGLISH)
        }
    }
}
