package com.google.jetstream.data.repositories

import com.google.jetstream.data.remote.BrewApiService
import com.google.jetstream.data.remote.BrewAuthCodeRequest
import com.google.jetstream.data.remote.BrewAuthConsumeRequest
import com.google.jetstream.data.remote.BrewAuthUserDto
import com.google.jetstream.data.remote.BrewSessionStore
import com.google.jetstream.data.remote.BrewUserDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val brewApiService: BrewApiService,
    private val sessionStore: BrewSessionStore,
) : AuthRepository {

    private val _currentUser = MutableStateFlow<BrewUserDto?>(null)
    override val currentUser: StateFlow<BrewUserDto?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    override suspend fun generateQrCode(): Result<Pair<String, Long>> = authRun("Could not generate QR code") {
        val response = brewApiService.generateQrCode()
        if (!response.success && response.code.isNullOrBlank()) {
            error(response.message ?: "Failed to generate QR code")
        }
        val code = response.code?.takeIf { it.isNotBlank() }
            ?: error(response.message ?: "Failed to generate QR code")
        val expiresAt = response.expiresAt ?: (System.currentTimeMillis() + 300_000L)
        code to expiresAt
    }

    override suspend fun pollQrCode(code: String): Result<QrPollResult> = authRun("QR poll failed") {
        val response = brewApiService.pollQrCode(code)
        QrPollResult(
            verified = response.verified,
            email = response.email,
            phone = response.phone,
        )
    }

    override suspend fun completeQrLogin(email: String?, phone: String?): Result<BrewUserDto> =
        authRun("Sign-in failed") {
            // Clear any stale tokens before starting a fresh SuperTokens flow.
            sessionStore.clear()

            val identifier = email?.takeIf { it.isNotBlank() }
                ?: phone?.takeIf { it.isNotBlank() }
                ?: error("Missing login identifier")

            val isEmail = identifier.contains('@')
            val send = brewApiService.sendAuthCode(
                BrewAuthCodeRequest(
                    isRNVerified = true,
                    email = if (isEmail) identifier else null,
                    phoneNumber = if (!isEmail) identifier else null,
                ),
            )
            if (send.status != null && send.status != "OK") {
                error("Sign-in failed: ${send.status}")
            }
            val deviceId = send.deviceId ?: error("Missing deviceId from server")
            val preAuthSessionId = send.preAuthSessionId ?: error("Missing preAuthSessionId from server")

            delay(500)

            var lastError: Throwable? = null
            repeat(CONSUME_MAX_ATTEMPTS) { attempt ->
                if (attempt > 0) delay(CONSUME_RETRY_DELAY_MS)
                try {
                    val consume = brewApiService.consumeAuthCode(
                        BrewAuthConsumeRequest(
                            userInputCode = TRUSTED_OTP,
                            deviceId = deviceId,
                            preAuthSessionId = preAuthSessionId,
                        ),
                    )

                    consume.user?.toBrewUserDto()?.let { user ->
                        _currentUser.value = user
                        _isLoggedIn.value = true
                        return@authRun user
                    }

                    if (!sessionStore.accessToken.isNullOrBlank()) {
                        delay(500)
                        val user = refreshUser().getOrThrow()
                        return@authRun user
                    }

                    if (consume.status != null && consume.status != "OK") {
                        error(consume.message ?: "Login failed: ${consume.status}")
                    }
                } catch (e: Throwable) {
                    lastError = e
                }
            }

            throw lastError ?: error("Login failed after QR scan")
        }

    override suspend fun refreshUser(): Result<BrewUserDto> = authRun("Could not load profile") {
        val response = brewApiService.getUserDetails()
        val user = response.user ?: error("Not logged in")
        _currentUser.value = user
        _isLoggedIn.value = true
        user
    }

    override fun logout() {
        sessionStore.clear()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    private fun BrewAuthUserDto.toBrewUserDto(): BrewUserDto = BrewUserDto(
        id = id,
        email = email,
        phone = phone,
        name = name ?: username,
    )

    private inline fun <T> authRun(fallback: String, block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string()?.take(200)
            Result.failure(
                Exception(
                    if (body.isNullOrBlank()) {
                        "$fallback (HTTP ${e.code()})"
                    } else {
                        "$fallback (HTTP ${e.code()}): $body"
                    },
                ),
            )
        } catch (e: Throwable) {
            Result.failure(e)
        }

    companion object {
        private const val TRUSTED_OTP = "1234"
        private const val CONSUME_MAX_ATTEMPTS = 3
        private const val CONSUME_RETRY_DELAY_MS = 600L
    }
}
