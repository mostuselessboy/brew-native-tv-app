package com.google.jetstream.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class BrewAuthInterceptor(
    private val sessionStore: BrewSessionStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val isAuthEndpoint = original.url.encodedPath.contains("/api/auth/")

        val builder = original.newBuilder()
            .header("Origin", BREW_ORIGIN)
            .header("Referer", "$BREW_ORIGIN/")

        if (isAuthEndpoint) {
            // Match supertokens-react-native header-based auth used by the RN tv-app.
            builder.header("rid", "passwordless")
            builder.header("st-auth-mode", "header")
            sessionStore.frontToken?.let { builder.header("front-token", it) }
            sessionStore.antiCsrf?.let { builder.header("anti-csrf", it) }
        }

        sessionStore.accessToken?.takeIf { it.isNotBlank() }?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(builder.build())
        sessionStore.updateFromResponse(response.headers)
        return response
    }

    companion object {
        private const val BREW_ORIGIN = "https://www.brew.tv"
    }
}
