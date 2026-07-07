package com.hdapp.feature.login.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdapp.core.ui.util.toUiText
import com.hdapp.domain.model.ApiResult
import com.hdapp.domain.model.AppError
import com.hdapp.domain.usecase.LoginUseCase
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

        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(error = AppError.ValidationError.toUiText()) }
            return
        }

        viewModelScope.launch {
            loginUseCase(currentState.username, currentState.password)
                .collect { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            _state.update { it.copy(isLoading = true, error = null) }
                        }
                        is ApiResult.Success -> {
                            _state.update { it.copy(isLoading = false, user = result.data) }
                            _effect.emit(LoginEffect.NavigateToDashboard)
                        }
                        is ApiResult.Error -> {
                            _state.update { 
                                it.copy(
                                    isLoading = false, 
                                    error = result.error.toUiText()
                                ) 
                            }
                        }
                    }
                }
        }
    }
}
