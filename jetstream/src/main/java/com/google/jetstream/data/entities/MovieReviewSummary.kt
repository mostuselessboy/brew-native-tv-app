package com.google.jetstream.data.entities

data class MovieReviewSummary(
    /** Average on 0–5 display scale. */
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0,
    /** Counts keyed 1–10 (half-star indices from API). */
    val ratingDistribution: Map<Int, Int> = emptyMap(),
)

enum class ReviewSection {
    CurrentCountry,
    OtherCountries,
    UserReview,
    ;

    companion object {
        fun fromApi(value: String?): ReviewSection = when (value) {
            "current_country" -> CurrentCountry
            "user_review" -> UserReview
            else -> OtherCountries
        }
    }
}
