package com.google.jetstream.data.repositories

import com.google.jetstream.data.remote.BrewUserDto
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<BrewUserDto?>
    val isLoggedIn: StateFlow<Boolean>
    suspend fun generateQrCode(): Result<Pair<String, Long>>
    suspend fun pollQrCode(code: String): Result<QrPollResult>
    suspend fun completeQrLogin(email: String?, phone: String?): Result<BrewUserDto>
    suspend fun refreshUser(): Result<BrewUserDto>
    fun logout()
}

data class QrPollResult(
    val verified: Boolean,
    val email: String? = null,
    val phone: String? = null,
)
