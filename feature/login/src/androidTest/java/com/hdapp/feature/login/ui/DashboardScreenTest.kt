package com.hdapp.feature.login.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.hdapp.core.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardScreen_displaysWelcomeMessage() {
        val username = "emilys"
        composeTestRule.setContent {
            AppTheme {
                DashboardScreen(
                    username = username,
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Welcome to Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hello, $username!").assertIsDisplayed()
    }
}
