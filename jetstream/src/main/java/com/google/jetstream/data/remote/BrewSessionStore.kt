package com.google.jetstream.data.remote

import javax.inject.Singleton

@Singleton
class BrewSessionStore {
    @Volatile var accessToken: String? = null
        private set

    @Volatile var refreshToken: String? = null
        private set

    /** SuperTokens front-token — must be forwarded between signinup/code and consume. */
    @Volatile var frontToken: String? = null
        private set

    /** SuperTokens anti-csrf — must be forwarded between signinup/code and consume. */
    @Volatile var antiCsrf: String? = null
        private set

    fun updateFromResponse(headers: okhttp3.Headers) {
        headers["st-access-token"]?.takeIf { it.isNotBlank() }?.let { accessToken = it }
        headers["st-refresh-token"]?.takeIf { it.isNotBlank() }?.let { refreshToken = it }
        headers["front-token"]?.takeIf { it.isNotBlank() }?.let { frontToken = it }
        headers["anti-csrf"]?.takeIf { it.isNotBlank() }?.let { antiCsrf = it }
    }

    fun clear() {
        accessToken = null
        refreshToken = null
        frontToken = null
        antiCsrf = null
    }
}
