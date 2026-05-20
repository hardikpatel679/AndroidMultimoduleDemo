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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.hdapp.androidmultimodule.navigation.AppNavGraph
import com.hdapp.core.ui.theme.AppLanguageConfig
import com.hdapp.core.ui.theme.AppTheme
import com.hdapp.core.ui.theme.AppThemeConfig
import com.hdapp.core.ui.theme.SettingsViewModel
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

            val isDarkTheme = when (settingsState.theme) {
                AppThemeConfig.DARK -> true
                AppThemeConfig.LIGHT -> false
                AppThemeConfig.SYSTEM -> isSystemInDarkTheme()
            }

            // Update Locale
            val context = LocalContext.current
            val currentConfiguration = LocalConfiguration.current
            
            val locale = if (settingsState.language == AppLanguageConfig.ARABIC) {
                Locale.forLanguageTag("ar")
            } else {
                Locale.forLanguageTag("en")
            }
            
            val configuration = Configuration(currentConfiguration)
            configuration.setLocale(locale)
            // update direction
            configuration.setLayoutDirection(locale)
            val localizedContext = context.createConfigurationContext(configuration)

            // Wrap the localized context to preserve the Activity context for Hilt
            val wrappedContext = object : ContextWrapper(localizedContext) {
                override fun getBaseContext(): Context {
                    return context
                }
            }

            CompositionLocalProvider(
                LocalContext provides wrappedContext,
                LocalConfiguration provides configuration
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
