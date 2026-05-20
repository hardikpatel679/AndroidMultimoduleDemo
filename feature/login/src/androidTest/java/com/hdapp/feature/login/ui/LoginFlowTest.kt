package com.hdapp.feature.login.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.hdapp.core.ui.theme.AppTheme
import com.hdapp.feature.login.mvi.LoginIntent
import com.hdapp.feature.login.mvi.LoginState
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class LoginFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun typingUsername_triggersIntent() {
        val onIntent: (LoginIntent) -> Unit = mockk(relaxed = true)
        
        composeTestRule.setContent {
            AppTheme {
                LoginContent(
                    state = LoginState(),
                    onIntent = onIntent
                )
            }
        }

        val username = "testuser"
        composeTestRule.onNodeWithText("Username").performTextInput(username)
        
        verify { onIntent(LoginIntent.UsernameChanged(username)) }
    }

    @Test
    fun clickingLogin_triggersLoginIntent() {
        val onIntent: (LoginIntent) -> Unit = mockk(relaxed = true)
        
        composeTestRule.setContent {
            AppTheme {
                LoginContent(
                    state = LoginState(username = "emilys", password = "pass"),
                    onIntent = onIntent
                )
            }
        }

        composeTestRule.onNodeWithText("Login").performClick()
        
        verify { onIntent(LoginIntent.LoginClicked) }
    }
    
    @Test
    fun togglingPasswordVisibility_triggersIntent() {
        val onIntent: (LoginIntent) -> Unit = mockk(relaxed = true)
        
        composeTestRule.setContent {
            AppTheme {
                LoginContent(
                    state = LoginState(),
                    onIntent = onIntent
                )
            }
        }

        // Click the visibility icon (it has a content description "Show password")
        composeTestRule.onNodeWithContentDescription("Show password").performClick()
        
        verify { onIntent(LoginIntent.TogglePasswordVisibility) }
    }
}
