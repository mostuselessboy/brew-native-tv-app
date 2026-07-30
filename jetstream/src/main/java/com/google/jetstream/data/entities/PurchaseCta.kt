package com.google.jetstream.data.entities

data class PurchaseCtaSlot(
    val kind: String,
    val color: String = "white",
    val isContinueWatching: Boolean = false,
    val percentageWatched: Float = 0f,
    val isFree: Boolean = false,
)

data class PurchaseCta(
    val scenario: String = "",
    val slots: List<PurchaseCtaSlot> = emptyList(),
)
