package com.hdapp.core.ui.theme

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hdapp.domain.model.AppLanguageConfig
import com.hdapp.domain.model.AppThemeConfig
import com.hdapp.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var repository: SettingsRepository
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        
        every { repository.getTheme() } returns flowOf(AppThemeConfig.LIGHT)
        every { repository.getLanguage() } returns flowOf(AppLanguageConfig.ENGLISH)
        
        viewModel = SettingsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state reflects repository values`() = runTest {
        viewModel.state.test {
            val item = awaitItem()
            assertThat(item.theme).isEqualTo(AppThemeConfig.LIGHT)
            assertThat(item.language).isEqualTo(AppLanguageConfig.ENGLISH)
        }
    }

    @Test
    fun `onThemeChange calls repository`() = runTest {
        viewModel.onThemeChange(AppThemeConfig.DARK)
        coVerify { repository.setTheme(AppThemeConfig.DARK) }
    }

    @Test
    fun `onLanguageChange calls repository`() = runTest {
        viewModel.onLanguageChange(AppLanguageConfig.ARABIC)
        coVerify { repository.setLanguage(AppLanguageConfig.ARABIC) }
    }
}
