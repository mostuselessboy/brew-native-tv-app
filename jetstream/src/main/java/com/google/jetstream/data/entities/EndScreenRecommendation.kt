package com.google.jetstream.data.entities

/** Flattened end-screen card — mobile `EndScreenRecommendationPick` parity. */
data class EndScreenRecommendation(
    val slug: String,
    val title: String,
    val posterUri: String,
    val action: EndScreenAction,
    val vodAssetId: Int? = null,
    val trailerUrl: String? = null,
)

enum class EndScreenAction {
    Player,
    Trailer,
    Detail,
    ;

    companion object {
        fun fromApi(value: String?): EndScreenAction = when (value) {
            "player" -> Player
            "trailer" -> Trailer
            else -> Detail
        }
    }
}
