package com.hdapp.feature.login.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.hdapp.feature.login.R
import com.hdapp.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

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
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            ThemeOption(stringResource(R.string.settings_theme_light), state.theme == AppThemeConfig.LIGHT) {
                viewModel.onThemeChange(AppThemeConfig.LIGHT)
            }
            ThemeOption(stringResource(R.string.settings_theme_dark), state.theme == AppThemeConfig.DARK) {
                viewModel.onThemeChange(AppThemeConfig.DARK)
            }
            ThemeOption(stringResource(R.string.settings_theme_system), state.theme == AppThemeConfig.SYSTEM) {
                viewModel.onThemeChange(AppThemeConfig.SYSTEM)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LanguageOption(stringResource(R.string.settings_language_english), state.language == AppLanguageConfig.ENGLISH) {
                viewModel.onLanguageChange(AppLanguageConfig.ENGLISH)
            }
            LanguageOption(stringResource(R.string.settings_language_arabic), state.language == AppLanguageConfig.ARABIC) {
                viewModel.onLanguageChange(AppLanguageConfig.ARABIC)
            }
        }
    }
}

@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
fun LanguageOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        RadioButton(selected = selected, onClick = onClick)
    }
}
