package com.hdapp.feature.login.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.hdapp.core.ui.theme.AppTheme
import com.hdapp.feature.login.mvi.LoginState
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginContent_displaysAllFields() {
        composeTestRule.setContent {
            AppTheme {
                LoginContent(
                    state = LoginState(),
                    onIntent = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithTag("LoginButton").assertIsDisplayed()
    }

    @Test
    fun loginContent_showsLoading_whenStateIsLoading() {
        composeTestRule.setContent {
            AppTheme {
                LoginContent(
                    state = LoginState(isLoading = true),
                    onIntent = {}
                )
            }
        }

        // Check if button is disabled when loading
        composeTestRule.onNodeWithTag("LoginButton").assertIsNotEnabled()
    }
}
