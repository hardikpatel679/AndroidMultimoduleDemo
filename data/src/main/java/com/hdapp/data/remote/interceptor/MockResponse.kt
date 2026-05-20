package com.hdapp.data.remote.interceptor

import com.hdapp.data.remote.api.Endpoints

data class MockResponse(
    val code: Int = 200,
    val assetPath: String,
    val contentType: String = "application/json"
)

object MockRegistry {
    private val mocks = mutableMapOf<String, MockResponse>()

    fun register(path: String, response: MockResponse) {
        mocks[path] = response
    }

    fun getResponse(url: String): MockResponse? {
        return mocks.entries.find { url.contains(it.key) }?.value
    }

    init {
        // Register initial mocks with asset paths
        register(
            Endpoints.LOGIN,
            MockResponse(assetPath = "mocks/auth/login_success.json")
        )
    }
}
