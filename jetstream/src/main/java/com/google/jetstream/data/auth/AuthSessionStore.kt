package com.google.jetstream.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AuthSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<BrewUser?>(null)
    val currentUser: StateFlow<BrewUser?> = _currentUser.asStateFlow()

    init {
        restoreCachedUser()
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var frontToken: String?
        get() = prefs.getString(KEY_FRONT_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_FRONT_TOKEN, value).apply()

    var cachedUserName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var cachedUserEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var cachedUserPhone: String?
        get() = prefs.getString(KEY_USER_PHONE, null)
        set(value) = prefs.edit().putString(KEY_USER_PHONE, value).apply()

    var cachedUserId: Int?
        get() = prefs.getInt(KEY_USER_ID, -1).takeIf { it >= 0 }
        set(value) = prefs.edit().apply {
            if (value == null || value < 0) remove(KEY_USER_ID) else putInt(KEY_USER_ID, value)
        }.apply()

    var cachedUserPicture: String?
        get() = prefs.getString(KEY_USER_PICTURE, null)
        set(value) = prefs.edit().putString(KEY_USER_PICTURE, value).apply()

    fun currentUserId(): Int? = currentUser.value?.id ?: cachedUserId

    fun isAuthenticated(): Boolean = !accessToken.isNullOrBlank()

    fun clear() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    fun saveUser(user: BrewUser) {
        cachedUserName = user.displayName
        cachedUserEmail = user.email
        cachedUserPhone = user.phone
        cachedUserId = user.id
        cachedUserPicture = user.picture
            ?: user.avatarUrl
            ?: user.profileImageUrl
        _currentUser.value = user
    }

    private fun restoreCachedUser() {
        if (!isAuthenticated()) {
            _currentUser.value = null
            return
        }
        val name = cachedUserName
        val email = cachedUserEmail
        val phone = cachedUserPhone
        if (!name.isNullOrBlank() || !email.isNullOrBlank() || !phone.isNullOrBlank()) {
            _currentUser.value = BrewUser(
                id = cachedUserId,
                name = name,
                email = email,
                phone = phone,
                picture = cachedUserPicture,
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "brew_auth_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_FRONT_TOKEN = "front_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_PICTURE = "user_picture"
    }
}
