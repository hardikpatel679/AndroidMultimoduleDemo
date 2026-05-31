package com.hdapp.domain.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScreenTest {

    @Test
    fun `Verify Login object`() {
        val screen = Screen.Login
        assertThat(screen).isNotNull()
    }

    @Test
    fun `Verify Settings object`() {
        val screen = Screen.Settings
        assertThat(screen).isNotNull()
    }

    @Test
    fun `Verify Dashboard data class`() {
        val username = "testuser"
        val screen = Screen.Dashboard(username)
        assertThat(screen.username).isEqualTo(username)
    }
}
