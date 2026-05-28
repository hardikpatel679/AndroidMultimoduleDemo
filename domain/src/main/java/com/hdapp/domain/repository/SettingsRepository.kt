package com.hdapp.domain.repository

import com.hdapp.domain.model.AppLanguageConfig
import com.hdapp.domain.model.AppThemeConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getTheme(): Flow<AppThemeConfig>
    suspend fun setTheme(theme: AppThemeConfig)
    fun getLanguage(): Flow<AppLanguageConfig>
    suspend fun setLanguage(language: AppLanguageConfig)
}
