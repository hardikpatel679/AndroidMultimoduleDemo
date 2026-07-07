package com.hdapp.feature.login.mvi

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hdapp.domain.model.ApiResult
import com.hdapp.domain.model.AppError
import com.hdapp.domain.model.User
import com.hdapp.domain.usecase.LoginUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

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
        every { loginUseCase(any(), any()) } returns flowOf(ApiResult.Error(AppError.Unauthorized))
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
        every { loginUseCase("newuser", "newpass") } returns flowOf(ApiResult.Loading, ApiResult.Success(user))

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
        every { loginUseCase("newuser", "newpass") } returns flowOf(ApiResult.Loading, ApiResult.Error(AppError.Unauthorized))

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
    fun `LoginClicked validation error updates state`() = runTest {
        every { loginUseCase(" ", " ") } returns flowOf(ApiResult.Loading, ApiResult.Error(AppError.ValidationError))

        viewModel.onIntent(LoginIntent.UsernameChanged(" "))
        viewModel.onIntent(LoginIntent.PasswordChanged(" "))

        viewModel.state.test {
            awaitItem() // Current after updates
            viewModel.onIntent(LoginIntent.LoginClicked)
            
            assertThat(awaitItem().isLoading).isTrue()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).isNotNull()
        }
    }

    @Test
    fun `LoginClicked does nothing if already loading`() = runTest {
        every { loginUseCase(any(), any()) } returns kotlinx.coroutines.flow.flow {
            emit(ApiResult.Loading)
            kotlinx.coroutines.delay(1000.milliseconds)
            emit(ApiResult.Success(mockk()))
        }

        viewModel.onIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceTimeBy(100)
        assertThat(viewModel.state.value.isLoading).isTrue()

        // Second click
        viewModel.onIntent(LoginIntent.LoginClicked)
        
        // Verify use case was only called once
        verify(exactly = 1) { 
            val flow = loginUseCase(any(), any())
            assertThat(flow).isNotNull()
        }
    }
}
