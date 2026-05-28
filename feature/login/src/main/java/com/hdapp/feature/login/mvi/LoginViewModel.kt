package com.hdapp.feature.login.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdapp.core.ui.util.UiText
import com.hdapp.domain.model.AppError
import com.hdapp.domain.usecase.LoginUseCase
import com.hdapp.feature.login.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UsernameChanged -> _state.update { it.copy(username = intent.username) }
            is LoginIntent.PasswordChanged -> _state.update { it.copy(password = intent.password) }
            LoginIntent.TogglePasswordVisibility -> _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            LoginIntent.LoginClicked -> handleLogin()
            LoginIntent.ClearError -> {
                if (_state.value.error != null) {
                    _state.update { it.copy(error = null) }
                }
            }
        }
    }

    private fun handleLogin() {
        val currentState = _state.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            loginUseCase(currentState.username, currentState.password)
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false, user = user) }
                    _effect.emit(LoginEffect.NavigateToDashboard)
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = mapErrorToUiText(throwable)) }
                }
        }
    }

    private fun mapErrorToUiText(throwable: Throwable): UiText {
        return when (throwable) {
            is AppError.NetworkError -> UiText.StringResource(R.string.error_network)
            is AppError.ServerError -> UiText.StringResource(R.string.error_server)
            is AppError.Unauthorized -> UiText.StringResource(R.string.error_unauthorized)
            is AppError.BusinessError -> {
                if (throwable.code == "VALIDATION_ERROR") {
                    UiText.StringResource(R.string.error_empty_fields)
                } else {
                    UiText.DynamicString(throwable.message)
                }
            }
            else -> UiText.StringResource(R.string.error_unknown)
        }
    }
}
