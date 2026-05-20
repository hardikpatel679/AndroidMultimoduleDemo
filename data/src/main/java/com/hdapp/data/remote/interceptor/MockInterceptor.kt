package com.hdapp.data.remote.interceptor

import android.content.Context
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toUri().toString()
        
        val mockResponse = MockRegistry.getResponse(url)

        return if (mockResponse != null) {
            val jsonContent = try {
                context.assets.open(mockResponse.assetPath).bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                "{ \"error\": \"Mock file not found: ${mockResponse.assetPath}\" }"
            }

            Response.Builder()
                .request(request)
                .code(mockResponse.code)
                .protocol(Protocol.HTTP_2)
                .message("OK")
                .body(jsonContent.toResponseBody(mockResponse.contentType.toMediaType()))
                .addHeader("content-type", mockResponse.contentType)
                .build()
        } else {
            chain.proceed(request)
        }
    }
}
