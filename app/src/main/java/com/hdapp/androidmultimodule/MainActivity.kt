package com.hdapp.androidmultimodule

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.hdapp.androidmultimodule.navigation.AppNavGraph
import com.hdapp.core.ui.theme.AppTheme
import com.hdapp.core.ui.theme.SettingsViewModel
import com.hdapp.domain.model.AppLanguageConfig
import com.hdapp.domain.model.AppThemeConfig
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.state.collectAsState()

            // Optimize: isDarkTheme is derived from settingsState and system theme
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val isDarkTheme = remember(settingsState.theme, isSystemInDarkTheme) {
                when (settingsState.theme) {
                    AppThemeConfig.DARK -> true
                    AppThemeConfig.LIGHT -> false
                    AppThemeConfig.SYSTEM -> isSystemInDarkTheme
                }
            }

            val currentConfiguration = LocalConfiguration.current
            val context = LocalContext.current

            // Optimize: Use remember to avoid recreating the localized context and configuration
            // on every recomposition. It only recalculates when language or base config changes.
            val (localizedContext, localizedConfig) = remember(settingsState.language, currentConfiguration) {
                val locale = if (settingsState.language == AppLanguageConfig.ARABIC) {
                    Locale.forLanguageTag("ar")
                } else {
                    Locale.forLanguageTag("en")
                }

                val configuration = Configuration(currentConfiguration)
                configuration.setLocale(locale)
                configuration.setLayoutDirection(locale)
                
                val baseLocalizedContext = context.createConfigurationContext(configuration)

                // Wrap the localized context to preserve the Activity context for Hilt
                val wrapped = object : ContextWrapper(baseLocalizedContext) {
                    override fun getBaseContext(): Context {
                        return context
                    }
                }
                wrapped to configuration
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfig
            ) {
                AppTheme(
                    darkTheme = isDarkTheme,
                    language = settingsState.language
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}
