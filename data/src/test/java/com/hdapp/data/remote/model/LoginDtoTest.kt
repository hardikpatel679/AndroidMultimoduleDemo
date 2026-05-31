package com.hdapp.data.remote.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginDtoTest {

    @Test
    fun `LoginResponse toDomain maps correctly`() {
        val response = LoginResponse(
            id = 1,
            username = "user",
            email = "email",
            firstName = "first",
            lastName = "last",
            gender = "male",
            image = "url",
            accessToken = "access",
            refreshToken = "refresh"
        )
        
        val domain = response.toDomain()
        
        assertThat(domain.id).isEqualTo(response.id)
        assertThat(domain.username).isEqualTo(response.username)
        assertThat(domain.email).isEqualTo(response.email)
        assertThat(domain.firstName).isEqualTo(response.firstName)
        assertThat(domain.lastName).isEqualTo(response.lastName)
        assertThat(domain.gender).isEqualTo(response.gender)
        assertThat(domain.image).isEqualTo(response.image)
        assertThat(domain.token).isEqualTo(response.accessToken)
        assertThat(domain.refreshToken).isEqualTo(response.refreshToken)
    }
}
