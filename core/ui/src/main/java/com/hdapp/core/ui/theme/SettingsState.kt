package com.hdapp.core.ui.theme

enum class AppThemeConfig {
    DARK, LIGHT, SYSTEM
}

enum class AppLanguageConfig {
    ENGLISH, ARABIC
}

data class SettingsState(
    val theme: AppThemeConfig = AppThemeConfig.SYSTEM,
    val language: AppLanguageConfig = AppLanguageConfig.ENGLISH
)
