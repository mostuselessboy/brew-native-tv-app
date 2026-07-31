package com.google.jetstream.data.auth

object AuthCountries {
    data class Country(
        val code: String,
        val dialCode: String,
        val name: String,
    )

    val priority: List<Country> = listOf(
        Country("IN", "91", "India"),
        Country("US", "1", "United States"),
        Country("CA", "1", "Canada"),
        Country("GB", "44", "United Kingdom"),
        Country("FR", "33", "France"),
    )

    fun findByCode(code: String): Country? = priority.find { it.code == code }
}

object AuthValidation {
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun isValidEmail(value: String): Boolean = emailPattern.matches(value.trim())

    fun validatePhoneDigits(digits: String, countryCode: String): String? {
        val cleaned = digits.filter { it.isDigit() }
        if (cleaned.isEmpty()) return "Please enter a valid phone number"
        if (cleaned.startsWith('0')) return "Please enter a valid phone number"
        val expected = when (countryCode) {
            "IN", "US" -> 10
            "GB" -> 10
            "FR" -> 9
            else -> null
        }
        expected?.let { len ->
            if (cleaned.length != len) return "Please enter a valid phone number"
        } ?: run {
            if (cleaned.length !in 7..15) return "Please enter a valid phone number"
        }
        return null
    }

    fun expectedOtpLength(channel: AuthSignInMethod): Int = when (channel) {
        AuthSignInMethod.Email -> 6
        AuthSignInMethod.Phone -> 4
        AuthSignInMethod.Qr -> 4
    }

    fun maskPhone(e164: String): String {
        val digits = e164.filter { it.isDigit() || it == '+' }
        if (digits.length <= 4) return digits
        val last4 = digits.takeLast(4)
        val prefix = digits.dropLast(4)
        return "$prefix ···· $last4"
    }
}
