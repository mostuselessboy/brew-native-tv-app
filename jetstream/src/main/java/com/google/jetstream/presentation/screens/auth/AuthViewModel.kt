package com.google.jetstream.presentation.screens.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.auth.AuthCountries
import com.google.jetstream.data.auth.AuthRepository
import com.google.jetstream.data.auth.AuthResult
import com.google.jetstream.data.auth.AuthSignInMethod
import com.google.jetstream.data.auth.AuthSignInStep
import com.google.jetstream.data.auth.AuthValidation
import com.google.jetstream.data.auth.BrewUser
import com.google.jetstream.data.auth.OtpSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val method: AuthSignInMethod = AuthSignInMethod.Qr,
    val step: AuthSignInStep = AuthSignInStep.Input,
    val isLoading: Boolean = false,
    val isLoggingIn: Boolean = false,
    val errorMessage: String? = null,
    val qrCode: String? = null,
    val qrUrl: String? = null,
    val qrExpiresAt: Long? = null,
    val qrExpired: Boolean = false,
    val qrPollFailures: Int = 0,
    val phoneDigits: String = "",
    val email: String = "",
    val selectedCountry: AuthCountries.Country = AuthCountries.priority.first(),
    val otp: String = "",
    val otpIssueChannel: AuthSignInMethod? = null,
    val otpSentIdentifier: String? = null,
    val otpSentDisplay: String? = null,
    val deviceId: String? = null,
    val preAuthSessionId: String? = null,
    val resendCountdown: Int = 0,
    val resendAttempts: Int = 0,
    val verifyAttempts: Int = 0,
    val isSignedIn: Boolean = false,
    val user: BrewUser? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var qrPollJob: Job? = null
    private var resendJob: Job? = null
    private var otpSession: OtpSession? = null

    init {
        viewModelScope.launch {
            when (val result = authRepository.refreshAuthState()) {
                is AuthResult.Success -> {
                    result.data?.let { user ->
                        _uiState.update { it.copy(isSignedIn = true, user = user) }
                    }
                }
                is AuthResult.Error -> Unit
            }
        }
        selectMethod(parseInitialMethod(savedStateHandle.get<String>(AuthScreenRoute.MethodBundleKey)))
    }

    private fun parseInitialMethod(raw: String?): AuthSignInMethod = when (raw?.lowercase()) {
        "phone" -> AuthSignInMethod.Phone
        "email" -> AuthSignInMethod.Email
        else -> AuthSignInMethod.Qr
    }

    fun selectMethod(method: AuthSignInMethod) {
        if (_uiState.value.isSignedIn) return
        _uiState.update {
            it.copy(
                method = method,
                step = AuthSignInStep.Input,
                errorMessage = null,
                otp = "",
            )
        }
        if (method == AuthSignInMethod.Qr) {
            generateQrCode()
        }
    }

    fun sendPhoneOtp() {
        if (_uiState.value.method != AuthSignInMethod.Phone) {
            _uiState.update { it.copy(method = AuthSignInMethod.Phone) }
        }
        sendOtp()
    }

    fun generateQrCode() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    qrExpired = false,
                    qrPollFailures = 0,
                )
            }
            when (val result = authRepository.generateQrCode()) {
                is AuthResult.Success -> {
                    val (code, expiresAt) = result.data
                    val url = "https://www.brew.tv/tv2?code=$code"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            qrCode = code,
                            qrUrl = url,
                            qrExpiresAt = expiresAt,
                            qrExpired = false,
                            qrPollFailures = 0,
                        )
                    }
                    startQrPolling(code, expiresAt)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun startQrPolling(code: String, expiresAt: Long) {
        qrPollJob?.cancel()
        qrPollJob = viewModelScope.launch {
            while (true) {
                delay(QR_POLL_INTERVAL_MS)
                val state = _uiState.value
                if (state.isSignedIn || state.isLoggingIn) return@launch

                if (System.currentTimeMillis() >= expiresAt) {
                    _uiState.update {
                        it.copy(
                            qrExpired = true,
                            errorMessage = "QR code expired. Refresh to try again.",
                        )
                    }
                    return@launch
                }

                when (val result = authRepository.pollQrCode(code)) {
                    is AuthResult.Success -> {
                        val poll = result.data
                        _uiState.update { it.copy(qrPollFailures = 0) }
                        if (poll.success && (poll.verified || authRepository.isAuthenticated)) {
                            completeQrLogin(poll.email, poll.phone)
                            return@launch
                        }
                    }
                    is AuthResult.Error -> {
                        val failures = state.qrPollFailures + 1
                        _uiState.update { it.copy(qrPollFailures = failures) }
                        if (failures >= MAX_QR_POLL_FAILURES) {
                            _uiState.update {
                                it.copy(
                                    errorMessage = "Having trouble connecting. Refresh the QR code or sign in with phone.",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun completeQrLogin(email: String?, phone: String?) {
        viewModelScope.launch {
            qrPollJob?.cancel()
            _uiState.update { it.copy(isLoggingIn = true, errorMessage = null) }
            when (val result = authRepository.completeQrLogin(email, phone)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(isLoggingIn = false, isSignedIn = true, user = result.data)
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoggingIn = false, errorMessage = result.message) }
                    generateQrCode()
                }
            }
        }
    }

    fun updatePhoneDigits(value: String) {
        _uiState.update { it.copy(phoneDigits = value.filter { ch -> ch.isDigit() }.take(15), errorMessage = null) }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value.take(120), errorMessage = null) }
    }

    fun updateOtp(value: String) {
        val channel = _uiState.value.otpIssueChannel ?: _uiState.value.method
        val maxLen = AuthValidation.expectedOtpLength(channel)
        val digits = value.filter { it.isDigit() }.take(maxLen)
        _uiState.update { it.copy(otp = digits, errorMessage = null) }
        if (digits.length == maxLen) {
            verifyOtp(digits)
        }
    }

    fun selectCountry(country: AuthCountries.Country) {
        _uiState.update { it.copy(selectedCountry = country, phoneDigits = "") }
    }

    fun cycleCountry() {
        val countries = AuthCountries.priority
        val current = _uiState.value.selectedCountry
        val nextIndex = (countries.indexOfFirst { it.code == current.code } + 1) % countries.size
        selectCountry(countries[nextIndex])
    }

    fun sendOtp() {
        val state = _uiState.value
        viewModelScope.launch {
            val identifier = when (state.method) {
                AuthSignInMethod.Email -> {
                    val email = state.email.trim()
                    if (!AuthValidation.isValidEmail(email)) {
                        _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
                        return@launch
                    }
                    email
                }
                AuthSignInMethod.Phone -> {
                    val error = AuthValidation.validatePhoneDigits(state.phoneDigits, state.selectedCountry.code)
                    if (error != null) {
                        _uiState.update { it.copy(errorMessage = error) }
                        return@launch
                    }
                    "+${state.selectedCountry.dialCode}${state.phoneDigits}"
                }
                AuthSignInMethod.Qr -> return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.sendOtp(identifier)) {
                is AuthResult.Success -> {
                    otpSession = result.data
                    val display = if (state.method == AuthSignInMethod.Phone) {
                        AuthValidation.maskPhone(identifier)
                    } else {
                        identifier
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = AuthSignInStep.Otp,
                            otpIssueChannel = state.method,
                            otpSentIdentifier = identifier,
                            otpSentDisplay = display,
                            deviceId = result.data.deviceId,
                            preAuthSessionId = result.data.preAuthSessionId,
                            otp = "",
                            verifyAttempts = 0,
                        )
                    }
                    startResendCountdown()
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun verifyOtp(code: String = _uiState.value.otp) {
        val session = otpSession
        val channel = _uiState.value.otpIssueChannel ?: _uiState.value.method
        val expectedLen = AuthValidation.expectedOtpLength(channel)
        if (code.length != expectedLen || session == null) {
            _uiState.update {
                it.copy(
                    errorMessage = if (channel == AuthSignInMethod.Email) {
                        "Please enter a valid 6-digit OTP"
                    } else {
                        "Please enter a valid 4-digit OTP"
                    },
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = authRepository.verifyOtp(
                    otp = code,
                    deviceId = session.deviceId,
                    preAuthSessionId = session.preAuthSessionId,
                )
            ) {
                is AuthResult.Success -> {
                    resendJob?.cancel()
                    _uiState.update {
                        it.copy(isLoading = false, isSignedIn = true, user = result.data, otp = "")
                    }
                }
                is AuthResult.Error -> {
                    val attempts = _uiState.value.verifyAttempts + 1
                    if (attempts >= MAX_VERIFY_ATTEMPTS) {
                        resetOtpStep("Incorrect OTP. Please try again")
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message,
                                verifyAttempts = attempts,
                                otp = "",
                            )
                        }
                    }
                }
            }
        }
    }

    fun resendOtp() {
        val state = _uiState.value
        if (state.resendCountdown > 0 || state.resendAttempts >= MAX_RESEND_ATTEMPTS) return
        val identifier = state.otpSentIdentifier ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.signOutLocal()
            when (val result = authRepository.sendOtp(identifier)) {
                is AuthResult.Success -> {
                    otpSession = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            deviceId = result.data.deviceId,
                            preAuthSessionId = result.data.preAuthSessionId,
                            resendAttempts = state.resendAttempts + 1,
                            otp = "",
                            verifyAttempts = 0,
                        )
                    }
                    startResendCountdown()
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun resetOtpStep(message: String? = null) {
        resendJob?.cancel()
        otpSession = null
        _uiState.update {
            it.copy(
                step = AuthSignInStep.Input,
                otp = "",
                otpIssueChannel = null,
                otpSentIdentifier = null,
                otpSentDisplay = null,
                deviceId = null,
                preAuthSessionId = null,
                resendCountdown = 0,
                resendAttempts = 0,
                verifyAttempts = 0,
                errorMessage = message,
            )
        }
    }

    fun switchInputMode() {
        val next = if (_uiState.value.method == AuthSignInMethod.Phone) {
            AuthSignInMethod.Email
        } else {
            AuthSignInMethod.Phone
        }
        if (_uiState.value.step == AuthSignInStep.Otp) {
            resetOtpStep()
        }
        selectMethod(next)
    }

    private fun startResendCountdown(seconds: Int = RESEND_COOLDOWN_SECONDS) {
        resendJob?.cancel()
        _uiState.update { it.copy(resendCountdown = seconds) }
        resendJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1_000)
                remaining -= 1
                _uiState.update { it.copy(resendCountdown = remaining) }
            }
        }
    }

    override fun onCleared() {
        qrPollJob?.cancel()
        resendJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val MAX_VERIFY_ATTEMPTS = 4
        private const val MAX_RESEND_ATTEMPTS = 3
        private const val RESEND_COOLDOWN_SECONDS = 30
        private const val QR_POLL_INTERVAL_MS = 2_000L
        private const val MAX_QR_POLL_FAILURES = 5
    }
}
