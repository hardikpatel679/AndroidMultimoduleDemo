package com.hdapp.feature.login.mvi

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hdapp.domain.model.AppError
import com.hdapp.domain.model.User
import com.hdapp.domain.usecase.LoginUseCase
import io.mockk.coEvery
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
            viewModel.onIntent(LoginIntent.LoginClicked)
            
            assertThat(awaitItem().username).isEqualTo("newuser")
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
            viewModel.onIntent(LoginIntent.LoginClicked)
            
            awaitItem() // Initial state after updates
            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).isNotNull()
        }
    }
}
