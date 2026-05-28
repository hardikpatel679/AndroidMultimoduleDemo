package com.hdapp.core.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdapp.domain.model.AppLanguageConfig
import com.hdapp.domain.model.AppThemeConfig
import com.hdapp.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        repository.getTheme(),
        repository.getLanguage()
    ) { theme, language ->
        SettingsState(theme = theme, language = language)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    fun onThemeChange(theme: AppThemeConfig) {
        viewModelScope.launch {
            repository.setTheme(theme)
        }
    }

    fun onLanguageChange(language: AppLanguageConfig) {
        viewModelScope.launch {
            repository.setLanguage(language)
        }
    }
}
