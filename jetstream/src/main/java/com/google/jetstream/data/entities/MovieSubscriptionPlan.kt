package com.google.jetstream.data.entities

data class MovieSubscriptionPlan(
    val id: Int = 0,
    val name: String? = null,
    val price: Double = 0.0,
    val perceivedPrice: Double? = null,
    val currencySymbol: String? = null,
    val intervalUnit: String? = null,
    val intervalCount: Int? = null,
    val isActive: Boolean = true,
)
