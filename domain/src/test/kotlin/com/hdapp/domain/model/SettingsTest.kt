package com.hdapp.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettingsTest {

    @Test
    fun `Verify AppThemeConfig values`() {
        assertThat(AppThemeConfig.entries).hasSize(3)
        assertThat(AppThemeConfig.valueOf("DARK")).isEqualTo(AppThemeConfig.DARK)
        assertThat(AppThemeConfig.valueOf("LIGHT")).isEqualTo(AppThemeConfig.LIGHT)
        assertThat(AppThemeConfig.valueOf("SYSTEM")).isEqualTo(AppThemeConfig.SYSTEM)
    }

    @Test
    fun `Verify AppLanguageConfig values`() {
        assertThat(AppLanguageConfig.entries).hasSize(2)
        assertThat(AppLanguageConfig.valueOf("ENGLISH")).isEqualTo(AppLanguageConfig.ENGLISH)
        assertThat(AppLanguageConfig.valueOf("ARABIC")).isEqualTo(AppLanguageConfig.ARABIC)
    }
}
