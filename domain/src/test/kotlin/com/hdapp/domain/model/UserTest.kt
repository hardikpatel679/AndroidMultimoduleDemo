package com.hdapp.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserTest {

    @Test
    fun `User data class preserves properties`() {
        val user = User(
            id = 1,
            username = "user",
            email = "email",
            firstName = "first",
            lastName = "last",
            gender = "male",
            image = "url",
            token = "token",
            refreshToken = "refresh"
        )
        
        assertThat(user.id).isEqualTo(1)
        assertThat(user.username).isEqualTo("user")
        assertThat(user.email).isEqualTo("email")
        assertThat(user.firstName).isEqualTo("first")
        assertThat(user.lastName).isEqualTo("last")
        assertThat(user.gender).isEqualTo("male")
        assertThat(user.image).isEqualTo("url")
        assertThat(user.token).isEqualTo("token")
        assertThat(user.refreshToken).isEqualTo("refresh")
    }
}
