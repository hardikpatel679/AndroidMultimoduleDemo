package com.hdapp.feature.login.mvi

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hdapp.core.ui.util.UiText
import com.hdapp.domain.model.AppError
import com.hdapp.domain.model.User
import com.hdapp.domain.usecase.LoginUseCase
import com.hdapp.feature.login.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var loginUseCase: LoginUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        val state = viewModel.state.value
        assertThat(state.username).isEmpty()
        assertThat(state.password).isEmpty()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `UsernameChanged intent updates state`() = runTest {
        viewModel.onIntent(LoginIntent.UsernameChanged("newuser"))
        assertThat(viewModel.state.value.username).isEqualTo("newuser")
    }

    @Test
    fun `PasswordChanged intent updates state`() = runTest {
        viewModel.onIntent(LoginIntent.PasswordChanged("newpassword"))
        assertThat(viewModel.state.value.password).isEqualTo("newpassword")
    }

    @Test
    fun `TogglePasswordVisibility intent updates state`() = runTest {
        assertThat(viewModel.state.value.isPasswordVisible).isFalse()
        viewModel.onIntent(LoginIntent.TogglePasswordVisibility)
        assertThat(viewModel.state.value.isPasswordVisible).isTrue()
        viewModel.onIntent(LoginIntent.TogglePasswordVisibility)
        assertThat(viewModel.state.value.isPasswordVisible).isFalse()
    }

    @Test
    fun `ClearError intent clears error in state`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(AppError.Unauthorized)
        viewModel.onIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.error).isNotNull()
        
        viewModel.onIntent(LoginIntent.ClearError)
        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun `LoginClicked success updates state and emits effect`() = runTest {
        val user = User(
            id = 1,
            username = "mock_user",
            email = "mock@example.com",
            firstName = "Mock",
            lastName = "User",
            gender = "male",
            image = "https://dummyjson.com/icon/emilys/128",
            token = "mock_token",
            refreshToken = ""
        )
        coEvery { loginUseCase("newuser", "newpass") } returns Result.success(user)

        viewModel.onIntent(LoginIntent.UsernameChanged("newuser"))
        viewModel.onIntent(LoginIntent.PasswordChanged("newpass"))

        viewModel.state.test {
            awaitItem() // Initial after UsernameChanged/PasswordChanged updates
            viewModel.onIntent(LoginIntent.LoginClicked)
            
            assertThat(awaitItem().isLoading).isTrue()
            
            val successState = awaitItem()
            assertThat(successState.isLoading).isFalse()
            assertThat(successState.user).isEqualTo(user)
        }

        viewModel.effect.test {
            viewModel.onIntent(LoginIntent.LoginClicked)
            assertThat(awaitItem()).isEqualTo(LoginEffect.NavigateToDashboard)
        }
    }

    @Test
    fun `LoginClicked failure updates state with error`() = runTest {
        coEvery { loginUseCase("newuser", "newpass") } returns Result.failure(AppError.Unauthorized)

        viewModel.onIntent(LoginIntent.UsernameChanged("newuser"))
        viewModel.onIntent(LoginIntent.PasswordChanged("newpass"))

        viewModel.state.test {
            awaitItem() // current
            viewModel.onIntent(LoginIntent.LoginClicked)
            
            assertThat(awaitItem().isLoading).isTrue()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).isNotNull()
        }
    }

    @Test
    fun `LoginClicked NetworkError updates state with network error resource`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(AppError.NetworkError)

        viewModel.onIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val error = viewModel.state.value.error as UiText.StringResource
        assertThat(error.resId).isEqualTo(R.string.error_network)
    }

    @Test
    fun `LoginClicked ServerError updates state with server error resource`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(AppError.ServerError)

        viewModel.onIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val error = viewModel.state.value.error as UiText.StringResource
        assertThat(error.resId).isEqualTo(R.string.error_server)
    }

    @Test
    fun `LoginClicked BusinessError Validation updates state with correct resource`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(AppError.BusinessError("VALIDATION_ERROR", "Msg"))

        viewModel.onIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val error = viewModel.state.value.error as UiText.StringResource
        assertThat(error.resId).isEqualTo(R.string.error_empty_fields)
    }

    @Test
    fun `LoginClicked Generic BusinessError updates state with dynamic string`() = runTest {
        val msg = "Generic Msg"
        coEvery { loginUseCase(any(), any()) } returns Result.failure(AppError.BusinessError("OTHER", msg))

        viewModel.onIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val error = viewModel.state.value.error as UiText.DynamicString
        assertThat(error.value).isEqualTo(msg)
    }

    @Test
    fun `LoginClicked unexpected error updates state with unknown error resource`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(RuntimeException())

        viewModel.onIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val error = viewModel.state.value.error as UiText.StringResource
        assertThat(error.resId).isEqualTo(R.string.error_unknown)
    }

    @Test
    fun `LoginClicked does nothing if already loading`() = runTest {
        coEvery { loginUseCase(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            Result.success(mockk())
        }

        viewModel.onIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceTimeBy(100)
        assertThat(viewModel.state.value.isLoading).isTrue()

        // Second click
        viewModel.onIntent(LoginIntent.LoginClicked)
        
        // Verify use case was only called once
        coVerify(exactly = 1) { loginUseCase(any(), any()) }
    }
}
