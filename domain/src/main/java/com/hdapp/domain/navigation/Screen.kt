package com.hdapp.domain.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Login : Screen
    @Serializable
    data object  Settings: Screen
    @Serializable
    data class Dashboard(val username: String) : Screen
}
