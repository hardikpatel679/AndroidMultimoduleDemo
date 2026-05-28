package com.hdapp.feature.login.mvi

import androidx.compose.runtime.Immutable
import com.hdapp.core.ui.util.UiText
import com.hdapp.domain.model.User

sealed class LoginIntent {
    data class UsernameChanged(val username: String) : LoginIntent()
    data class PasswordChanged(val password: String) : LoginIntent()
    object TogglePasswordVisibility : LoginIntent()
    object LoginClicked : LoginIntent()
    object ClearError : LoginIntent()
}

@Immutable
data class LoginState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val user: User? = null
)

sealed class LoginEffect {
    data class ShowToast(val message: String) : LoginEffect()
    object NavigateToDashboard : LoginEffect()
}
