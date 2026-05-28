package com.hdapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hdapp.domain.model.AppLanguageConfig
import com.hdapp.domain.model.AppThemeConfig
import com.hdapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
    }

    override fun getTheme(): Flow<AppThemeConfig> {
        return dataStore.data.map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME] ?: AppThemeConfig.SYSTEM.name
            AppThemeConfig.valueOf(themeName)
        }
    }

    override suspend fun setTheme(theme: AppThemeConfig) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    override fun getLanguage(): Flow<AppLanguageConfig> {
        return dataStore.data.map { preferences ->
            val languageName = preferences[PreferencesKeys.LANGUAGE] ?: AppLanguageConfig.ENGLISH.name
            AppLanguageConfig.valueOf(languageName)
        }
    }

    override suspend fun setLanguage(language: AppLanguageConfig) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.name
        }
    }
}
