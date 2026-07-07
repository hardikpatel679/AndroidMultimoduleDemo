package com.hdapp.androidmultimodule.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.hdapp.core.ui.theme.SettingsViewModel
import com.hdapp.domain.navigation.Screen
import com.hdapp.feature.login.mvi.LoginViewModel
import com.hdapp.feature.login.ui.DashboardScreen
import com.hdapp.feature.login.ui.LoginScreen
import com.hdapp.feature.login.ui.SettingsScreen

@Composable
fun AppNavGraph() {
    val backStack = rememberNavBackStack(Screen.Login)

    NavDisplay(
        backStack = backStack,
        onBack = { 
            if (backStack.size > 1) {
                backStack.removeAt(backStack.size - 1) 
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { key ->
            when (val screen = key as Screen) {
                is Screen.Login -> {
                    NavEntry(screen) {
                        val loginViewModel: LoginViewModel = hiltViewModel()
                        LoginScreen(
                            viewModel = loginViewModel,
                            onNavigateToDashboard = { username ->
                                backStack.add(Screen.Dashboard(username))
                            }
                        )
                    }
                }
                is Screen.Dashboard -> {
                    NavEntry(screen) {
                        DashboardScreen(
                            username = screen.username,
                            onNavigateToSettings = {
                                backStack.add(Screen.Settings)
                            }
                        )
                    }
                }
                is Screen.Settings -> {
                    NavEntry(screen) {
                        val settingsViewModel: SettingsViewModel = hiltViewModel()
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { backStack.removeAt(backStack.size - 1) }
                        )
                    }
                }
            }
        }
    )
}
