package com.hdapp.data.remote.interceptor

import android.content.Context
import android.content.res.AssetManager
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class MockInterceptorTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var interceptor: MockInterceptor
    private lateinit var chain: Interceptor.Chain

    @Before
    fun setUp() {
        context = mockk()
        assetManager = mockk()
        every { context.assets } returns assetManager
        interceptor = MockInterceptor(context)
        chain = mockk()
    }

    @Test
    fun `intercept returns mock response when path registered`() {
        val url = "https://example.com/auth/login"
        val request = Request.Builder().url(url).build()
        val mockJson = "{\"status\": \"success\"}"
        
        every { chain.request() } returns request
        every { assetManager.open(any()) } returns ByteArrayInputStream(mockJson.toByteArray())

        val response = interceptor.intercept(chain)

        assertThat(response.code).isEqualTo(200)
        assertThat(response.body?.string()).isEqualTo(mockJson)
    }

    @Test
    fun `intercept calls proceed when path not registered`() {
        val url = "https://example.com/other/path"
        val request = Request.Builder().url(url).build()
        val expectedResponse = mockk<Response>()
        
        every { chain.request() } returns request
        every { chain.proceed(any()) } returns expectedResponse

        val response = interceptor.intercept(chain)

        assertThat(response).isEqualTo(expectedResponse)
    }

    @Test
    fun `MockRegistry allows dynamic registration`() {
        val path = "test/path"
        val mockResponse = MockResponse(assetPath = "test.json", code = 201)
        
        MockRegistry.register(path, mockResponse)
        
        val result = MockRegistry.getResponse("http://host.com/$path")
        assertThat(result).isEqualTo(mockResponse)
    }
}
