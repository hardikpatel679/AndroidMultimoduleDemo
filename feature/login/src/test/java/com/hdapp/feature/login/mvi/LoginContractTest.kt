package com.hdapp.feature.login.mvi

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginContractTest {

    @Test
    fun `LoginState default values are correct`() {
        val state = LoginState()
        assertThat(state.username).isEmpty()
        assertThat(state.password).isEmpty()
        assertThat(state.isPasswordVisible).isFalse()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.user).isNull()
    }

    @Test
    fun `LoginIntent UsernameChanged preserves value`() {
        val username = "test"
        val intent = LoginIntent.UsernameChanged(username)
        assertThat(intent.username).isEqualTo(username)
    }

    @Test
    fun `LoginIntent PasswordChanged preserves value`() {
        val password = "pass"
        val intent = LoginIntent.PasswordChanged(password)
        assertThat(intent.password).isEqualTo(password)
    }

    @Test
    fun `LoginEffect ShowToast preserves message`() {
        val message = "Hello"
        val effect = LoginEffect.ShowToast(message)
        assertThat(effect.message).isEqualTo(message)
    }
}
