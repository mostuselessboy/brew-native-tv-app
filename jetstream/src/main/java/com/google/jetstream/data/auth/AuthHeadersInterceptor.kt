package com.google.jetstream.data.auth

import okhttp3.Interceptor
import okhttp3.Response

/** Brew.tv + SuperTokens headers required for auth API calls. */
class AuthHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Referer", "https://www.brew.tv/")
            .header("Origin", "https://www.brew.tv")
            .header("st-auth-mode", "header")
            .build()
        return chain.proceed(request)
    }
}
