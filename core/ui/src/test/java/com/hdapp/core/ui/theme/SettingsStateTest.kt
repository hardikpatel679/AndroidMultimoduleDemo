package com.hdapp.core.ui.theme

import com.google.common.truth.Truth.assertThat
import com.hdapp.domain.model.AppLanguageConfig
import com.hdapp.domain.model.AppThemeConfig
import org.junit.Test

class SettingsStateTest {

    @Test
    fun `SettingsState default values are correct`() {
        val state = SettingsState()
        assertThat(state.theme).isEqualTo(AppThemeConfig.SYSTEM)
        assertThat(state.language).isEqualTo(AppLanguageConfig.ENGLISH)
    }

    @Test
    fun `SettingsState preserves custom values`() {
        val state = SettingsState(theme = AppThemeConfig.DARK, language = AppLanguageConfig.ARABIC)
        assertThat(state.theme).isEqualTo(AppThemeConfig.DARK)
        assertThat(state.language).isEqualTo(AppLanguageConfig.ARABIC)
    }
}
