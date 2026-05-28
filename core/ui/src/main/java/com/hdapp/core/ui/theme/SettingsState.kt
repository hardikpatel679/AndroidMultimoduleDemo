package com.hdapp.core.ui.theme

import androidx.compose.runtime.Immutable
import com.hdapp.domain.model.AppLanguageConfig
import com.hdapp.domain.model.AppThemeConfig

@Immutable
data class SettingsState(
    val theme: AppThemeConfig = AppThemeConfig.SYSTEM,
    val language: AppLanguageConfig = AppLanguageConfig.ENGLISH
)
