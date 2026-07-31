package com.google.jetstream.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

data class OtpSession(
    val deviceId: String,
    val preAuthSessionId: String,
    val flowType: String? = null,
)

@Singleton
class AuthRepository @Inject constructor(
    @AuthClient private val authClient: OkHttpClient,
    private val sessionStore: AuthSessionStore,
    private val json: Json,
) {
    val isAuthenticated: Boolean
        get() = sessionStore.isAuthenticated()

    val cachedUserName: String?
        get() = sessionStore.cachedUserName

    suspend fun generateQrCode(): AuthResult<Pair<String, Long>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$BASE_URL/api/v1/auth/qr-code/generate")
                .post("{}".toRequestBody(JSON))
                .build()
            val response = authClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            val parsed = json.decodeFromString<QrGenerateResponse>(body)
            if (parsed.success && parsed.code != null && parsed.expiresAt != null) {
                AuthResult.Success(parsed.code to parsed.expiresAt)
            } else {
                AuthResult.Error(parsed.message ?: "Failed to generate QR code")
            }
        }.getOrElse {
            AuthResult.Error(it.message ?: "Failed to generate QR code")
        }
    }

    suspend fun pollQrCode(code: String): AuthResult<QrPollResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$BASE_URL/api/v1/auth/qr-code/poll/$code")
                .get()
                .build()
            val response = authClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return@runCatching AuthResult.Error("Failed to poll QR code (${response.code})")
            }
            val parsed = json.decodeFromString<QrPollResponse>(body)
            if (!parsed.success) {
                AuthResult.Error(parsed.message ?: "Failed to poll QR code")
            } else {
                AuthResult.Success(parsed)
            }
        }.getOrElse {
            AuthResult.Error(it.message ?: "Failed to poll QR code")
        }
    }

    /** TV QR flow — uses dummy OTP after phone scan (tv-app parity). */
    suspend fun completeQrLogin(email: String?, phone: String?): AuthResult<BrewUser> {
        val identifier = when {
            !phone.isNullOrBlank() -> phone
            !email.isNullOrBlank() -> email
            else -> return AuthResult.Error("Missing login identifier")
        }
        return when (val sent = sendOtp(identifier, tvVerified = true)) {
            is AuthResult.Error -> sent
            is AuthResult.Success -> {
                kotlinx.coroutines.delay(400)
                when (
                    val verified = verifyOtp(
                        otp = TV_DUMMY_OTP,
                        deviceId = sent.data.deviceId,
                        preAuthSessionId = sent.data.preAuthSessionId,
                    )
                ) {
                    is AuthResult.Error -> verified
                    is AuthResult.Success -> AuthResult.Success(verified.data)
                }
            }
        }
    }

    /** Phone/email OTP — real delivery (mobile-viewer parity, no isRNVerified). */
    suspend fun sendOtp(identifier: String, tvVerified: Boolean = false): AuthResult<OtpSession> =
        withContext(Dispatchers.IO) {
            runCatching {
                val isEmail = identifier.contains('@')
                val payload = SendOtpRequest(
                    email = if (isEmail) identifier.trim() else null,
                    phoneNumber = if (isEmail) null else identifier,
                    isRnVerified = if (tvVerified) true else null,
                )
                val request = Request.Builder()
                    .url("$BASE_URL/api/auth/signinup/code")
                    .post(json.encodeToString(payload).toRequestBody(JSON))
                    .build()
                val response = authClient.newCall(request).execute()
                captureTokens(response)
                val body = response.body?.string().orEmpty()
                val parsed = json.decodeFromString<SendOtpResponse>(body)
                when (parsed.status) {
                    "OK" -> {
                        val deviceId = parsed.deviceId
                        val preAuthSessionId = parsed.preAuthSessionId
                        if (deviceId.isNullOrBlank() || preAuthSessionId.isNullOrBlank()) {
                            AuthResult.Error("Failed to send OTP. Please try again.")
                        } else {
                            AuthResult.Success(
                                OtpSession(
                                    deviceId = deviceId,
                                    preAuthSessionId = preAuthSessionId,
                                    flowType = parsed.flowType,
                                ),
                            )
                        }
                    }
                    "SIGN_IN_UP_NOT_ALLOWED" ->
                        AuthResult.Error(parsed.reason ?: parsed.message ?: "Sign in not allowed")
                    else -> AuthResult.Error("Failed to send OTP. Please try again.")
                }
            }.getOrElse {
                AuthResult.Error(it.message ?: "Failed to send OTP. Please try again.")
            }
        }

    suspend fun verifyOtp(
        otp: String,
        deviceId: String,
        preAuthSessionId: String,
    ): AuthResult<BrewUser> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = ConsumeOtpRequest(
                userInputCode = otp,
                deviceId = deviceId,
                preAuthSessionId = preAuthSessionId,
            )
            val request = Request.Builder()
                .url("$BASE_URL/api/auth/signinup/code/consume")
                .post(json.encodeToString(payload).toRequestBody(JSON))
                .build()
            val response = authClient.newCall(request).execute()
            captureTokens(response)
            val body = response.body?.string().orEmpty()
            val parsed = json.decodeFromString<ConsumeOtpResponse>(body)
            val sessionReady = parsed.status.equals("OK", ignoreCase = true) ||
                parsed.user != null ||
                sessionStore.isAuthenticated()
            if (sessionReady) {
                kotlinx.coroutines.delay(400)
                when (val details = fetchUserDetailsInternal()) {
                    is AuthResult.Success -> AuthResult.Success(details.data)
                    is AuthResult.Error -> {
                        if (sessionStore.isAuthenticated()) {
                            val fallback = BrewUser(
                                name = sessionStore.cachedUserName,
                                email = sessionStore.cachedUserEmail,
                                phone = sessionStore.cachedUserPhone,
                            )
                            sessionStore.saveUser(fallback)
                            AuthResult.Success(fallback)
                        } else {
                            AuthResult.Error("Login succeeded, but we could not load your account.")
                        }
                    }
                }
            } else {
                AuthResult.Error("Incorrect OTP")
            }
        }.getOrElse {
            AuthResult.Error("Incorrect OTP")
        }
    }

    suspend fun resendOtp(identifier: String, tvVerified: Boolean = false): AuthResult<OtpSession> {
        signOutLocal()
        return sendOtp(identifier, tvVerified = tvVerified)
    }

    suspend fun getUserDetails(): AuthResult<BrewUser> = withContext(Dispatchers.IO) {
        fetchUserDetailsInternal()
    }

    suspend fun refreshAuthState(): AuthResult<BrewUser?> = withContext(Dispatchers.IO) {
        if (!sessionStore.isAuthenticated()) {
            AuthResult.Success(null)
        } else {
            when (val details = fetchUserDetailsInternal()) {
                is AuthResult.Success -> AuthResult.Success(details.data)
                is AuthResult.Error -> {
                    signOutLocal()
                    AuthResult.Success(null)
                }
            }
        }
    }

    fun signOutLocal() {
        sessionStore.clear()
    }

    suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$BASE_URL/api/auth/signout")
                .post("{}".toRequestBody(JSON))
                .build()
            authClient.newCall(request).execute().close()
        }
        signOutLocal()
    }

    private fun fetchUserDetailsInternal(): AuthResult<BrewUser> {
        val token = sessionStore.accessToken
        if (token.isNullOrBlank()) {
            return AuthResult.Error("Not authenticated")
        }
        return runCatching {
            val request = Request.Builder()
                .url("$BASE_URL/api/v1/user/details")
                .get()
                .header("Authorization", "Bearer $token")
                .build()
            val response = authClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            val parsed = json.decodeFromString<UserDetailsResponse>(body)
            if (parsed.success && parsed.user != null) {
                sessionStore.saveUser(parsed.user)
                AuthResult.Success(parsed.user)
            } else {
                AuthResult.Error(parsed.message ?: "Failed to get user details")
            }
        }.getOrElse {
            AuthResult.Error(it.message ?: "Failed to get user details")
        }
    }

    private fun captureTokens(response: Response) {
        fun header(name: String): String? = response.header(name) ?: response.header(name.lowercase())
        header("st-access-token")?.let { sessionStore.accessToken = it }
        header("st-refresh-token")?.let { sessionStore.refreshToken = it }
        header("front-token")?.let { sessionStore.frontToken = it }
    }

    companion object {
        private const val BASE_URL = "https://www.brew.tv"
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private const val TV_DUMMY_OTP = "1234"
    }
}
