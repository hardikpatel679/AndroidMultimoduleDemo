package com.hdapp.feature.login.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hdapp.feature.login.R
import com.hdapp.core.ui.theme.*
import com.hdapp.domain.model.AppLanguageConfig
import com.hdapp.domain.model.AppThemeConfig

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    // Stable event handlers to prevent unnecessary recompositions of child rows
    val onThemeChange = remember(viewModel) {
        { theme: AppThemeConfig -> viewModel.onThemeChange(theme) }
    }
    val onLanguageChange = remember(viewModel) {
        { language: AppLanguageConfig -> viewModel.onLanguageChange(language) }
    }

    SettingsContent(
        state = state,
        onBack = onBack,
        onThemeChange = onThemeChange,
        onLanguageChange = onLanguageChange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsState,
    onBack: () -> Unit,
    onThemeChange: (AppThemeConfig) -> Unit,
    onLanguageChange: (AppLanguageConfig) -> Unit
) {
    val dimens = LocalDimens.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(dimens.paddingLarge)
        ) {
            // Theme Section
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = dimens.small)
            )
            
            AppThemeConfig.entries.forEach { theme ->
                ThemeOption(
                    theme = theme,
                    selected = state.theme == theme,
                    dimens = dimens,
                    onClick = onThemeChange
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = dimens.medium))

            // Language Section
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = dimens.small)
            )

            AppLanguageConfig.entries.forEach { lang ->
                LanguageOption(
                    language = lang,
                    selected = state.language == lang,
                    dimens = dimens,
                    onClick = onLanguageChange
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    theme: AppThemeConfig,
    selected: Boolean,
    dimens: Dimens,
    onClick: (AppThemeConfig) -> Unit
) {
    val text = stringResource(when(theme) {
        AppThemeConfig.LIGHT -> R.string.settings_theme_light
        AppThemeConfig.DARK -> R.string.settings_theme_dark
        AppThemeConfig.SYSTEM -> R.string.settings_theme_system
    })
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(theme) }
            .padding(vertical = dimens.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        RadioButton(selected = selected, onClick = { onClick(theme) })
    }
}

@Composable
private fun LanguageOption(
    language: AppLanguageConfig,
    selected: Boolean,
    dimens: Dimens,
    onClick: (AppLanguageConfig) -> Unit
) {
    val text = stringResource(when(language) {
        AppLanguageConfig.ENGLISH -> R.string.settings_language_english
        AppLanguageConfig.ARABIC -> R.string.settings_language_arabic
    })

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(language) }
            .padding(vertical = dimens.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        RadioButton(selected = selected, onClick = { onClick(language) })
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        SettingsContent(
            state = SettingsState(
                theme = AppThemeConfig.SYSTEM,
                language = AppLanguageConfig.ENGLISH
            ),
            onBack = {},
            onThemeChange = {},
            onLanguageChange = {}
        )
    }
}
