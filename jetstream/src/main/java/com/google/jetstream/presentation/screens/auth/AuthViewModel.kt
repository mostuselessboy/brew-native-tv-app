package com.google.jetstream.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn = authRepository.isLoggedIn
    val currentUser = authRepository.currentUser

    private var pollJob: Job? = null
    private var pendingEmail: String? = null
    private var pendingPhone: String? = null

    init {
        viewModelScope.launch {
            runCatching { authRepository.refreshUser() }
            if (authRepository.isLoggedIn.value) {
                _uiState.value = AuthUiState.AlreadyLoggedIn
                return@launch
            }
            generateQr()
        }
    }

    fun generateQr() {
        pollJob?.cancel()
        pendingEmail = null
        pendingPhone = null
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.generateQrCode()
                .onSuccess { (code, expiresAt) ->
                    val url = "https://www.brew.tv/tv2?code=$code"
                    _uiState.value = AuthUiState.Ready(
                        code = code,
                        qrUrl = url,
                        expiresAt = expiresAt,
                    )
                    startPolling(code)
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(
                        message = it.message ?: "Could not generate QR code",
                        canRetryLogin = false,
                    )
                }
        }
    }

    fun retryLogin() {
        if (pendingEmail == null && pendingPhone == null) {
            generateQr()
            return
        }
        finishLogin()
    }

    private fun startPolling(code: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(2_000)
                val poll = authRepository.pollQrCode(code).getOrNull() ?: continue
                if (poll.verified && (poll.email != null || poll.phone != null)) {
                    pollJob?.cancel()
                    pendingEmail = poll.email
                    pendingPhone = poll.phone
                    finishLogin()
                    return@launch
                }
            }
        }
    }

    private fun finishLogin() {
        pollJob?.cancel()
        _uiState.value = AuthUiState.LoggingIn
        viewModelScope.launch {
            authRepository.completeQrLogin(pendingEmail, pendingPhone)
                .onSuccess {
                    _uiState.value = AuthUiState.Success
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(
                        message = it.message ?: "Login failed after scan. Tap retry.",
                        canRetryLogin = true,
                    )
                }
        }
    }

    fun logout() {
        pollJob?.cancel()
        authRepository.logout()
        generateQr()
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object AlreadyLoggedIn : AuthUiState()
    data object LoggingIn : AuthUiState()
    data object Success : AuthUiState()
    data class Ready(
        val code: String,
        val qrUrl: String,
        val expiresAt: Long,
    ) : AuthUiState()
    data class Error(
        val message: String,
        val canRetryLogin: Boolean = false,
    ) : AuthUiState()
}
