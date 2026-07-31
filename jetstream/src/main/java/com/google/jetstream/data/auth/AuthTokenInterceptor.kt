package com.google.jetstream.data.auth

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Adds SuperTokens bearer token to authenticated Brew API calls. */
class AuthTokenInterceptor @Inject constructor(
    private val sessionStore: AuthSessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sessionStore.accessToken
        val builder = original.newBuilder()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
            builder.header("st-auth-mode", "header")
        }
        val response = chain.proceed(builder.build())
        captureTokens(response)
        return response
    }

    private fun captureTokens(response: Response) {
        fun header(name: String): String? = response.header(name) ?: response.header(name.lowercase())
        header("st-access-token")?.let { sessionStore.accessToken = it }
        header("st-refresh-token")?.let { sessionStore.refreshToken = it }
        header("front-token")?.let { sessionStore.frontToken = it }
    }
}
