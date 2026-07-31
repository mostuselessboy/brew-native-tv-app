package com.google.jetstream.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SendOtpRequest(
    val email: String? = null,
    @SerialName("phoneNumber") val phoneNumber: String? = null,
    @SerialName("isRNVerified") val isRnVerified: Boolean? = null,
)

@Serializable
data class SendOtpResponse(
    val status: String? = null,
    val deviceId: String? = null,
    val preAuthSessionId: String? = null,
    val flowType: String? = null,
    val reason: String? = null,
    val message: String? = null,
)

@Serializable
data class ConsumeOtpRequest(
    @SerialName("userInputCode") val userInputCode: String,
    val deviceId: String,
    @SerialName("preAuthSessionId") val preAuthSessionId: String,
)

@Serializable
data class ConsumeOtpResponse(
    val user: JsonObject? = null,
    val message: String? = null,
    val redirect: String? = null,
    val status: String? = null,
)

@Serializable
data class QrGenerateResponse(
    val success: Boolean = false,
    val code: String? = null,
    val expiresAt: Long? = null,
    val message: String? = null,
)

@Serializable
data class QrPollResponse(
    val success: Boolean = false,
    val verified: Boolean = false,
    val email: String? = null,
    val phone: String? = null,
    val message: String? = null,
)

@Serializable
data class UserDetailsResponse(
    val success: Boolean = false,
    val user: BrewUser? = null,
    val message: String? = null,
)

@Serializable
data class BrewUser(
    val id: Int? = null,
    val name: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val picture: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
) {
    val displayName: String
        get() = listOfNotNull(name?.trim(), lastName?.trim())
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { email ?: phone ?: "Brew member" }
}

enum class AuthSignInMethod {
    Qr,
    Phone,
    Email,
}

enum class AuthSignInStep {
    Input,
    Otp,
}
