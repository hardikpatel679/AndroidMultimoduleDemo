package com.hdapp.androidmultimodule.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.hdapp.domain.navigation.Screen
import com.hdapp.feature.login.mvi.LoginViewModel
import com.hdapp.feature.login.ui.DashboardScreen
import com.hdapp.feature.login.ui.LoginScreen

@Composable
fun AppNavGraph() {
    val backStack = rememberNavBackStack(Screen.Login)

    Scaffold(modifier = Modifier) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeAt(backStack.size - 1) },
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
                                DashboardScreen(username = screen.username)
                            }
                        }
                    }
                }
            )
        }
    }
}
