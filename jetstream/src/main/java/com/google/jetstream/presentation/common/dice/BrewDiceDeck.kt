package com.google.jetstream.presentation.common.dice

import com.google.jetstream.data.entities.Movie
import kotlin.math.min
import kotlin.random.Random

const val BREW_DICE_POOL_SIZE = 8

data class BrewDiceDeckMovie(
    val movie: Movie,
    val posterUri: String,
    val title: String,
    val tagline: String,
)

data class BrewDiceRoll(
    val deck: List<BrewDiceDeckMovie>,
    val winnerIndex: Int,
    val winner: BrewDiceDeckMovie,
)

private const val PRE_EXPAND_WING_COUNT = 4

fun buildDiceRoll(movies: List<Movie>, random: Random = Random.Default): BrewDiceRoll? {
    if (movies.isEmpty()) return null
    val pool = movies.shuffled(random).take(min(BREW_DICE_POOL_SIZE, movies.size))
    if (pool.isEmpty()) return null

    val winnerMovie = pool[random.nextInt(pool.size)]
    val deckMovies = pool.map { movie ->
        BrewDiceDeckMovie(
            movie = movie,
            posterUri = movie.backdropUri?.takeIf { it.isNotBlank() } ?: movie.posterUri,
            title = movie.name,
            tagline = movie.description.take(80).ifBlank { "Tonight's pick" },
        )
    }
    val winnerIndex = deckMovies.indexOfFirst { it.movie.id == winnerMovie.id }.coerceAtLeast(0)
    val arranged = arrangeDeckForStagedFan(deckMovies, winnerIndex)
    return BrewDiceRoll(
        deck = arranged.deck,
        winnerIndex = arranged.winnerIndex,
        winner = arranged.deck[arranged.winnerIndex],
    )
}

private fun arrangeDeckForStagedFan(
    deck: List<BrewDiceDeckMovie>,
    winnerIndex: Int,
): BrewDiceRoll {
    val count = deck.size
    if (count <= 1) {
        return BrewDiceRoll(deck = deck, winnerIndex = winnerIndex, winner = deck[winnerIndex])
    }

    val winner = deck[winnerIndex]
    val others = deck.filterIndexed { index, _ -> index != winnerIndex }
    val frontDecoy = others.firstOrNull() ?: return BrewDiceRoll(deck, winnerIndex, winner)

    val wingDecoys = others.drop(1).take(PRE_EXPAND_WING_COUNT)
    val lateDecoys = others.drop(1 + PRE_EXPAND_WING_COUNT)
    val winnerSlot = maxOf(3, count / 2)

    val ordered = arrayOfNulls<BrewDiceDeckMovie>(count)
    ordered[0] = frontDecoy
    ordered[winnerSlot] = winner
    if (wingDecoys.isNotEmpty()) ordered[1] = wingDecoys.getOrNull(0)
    if (wingDecoys.size > 1) ordered[2] = wingDecoys[1]
    if (wingDecoys.size > 2) ordered[count - 1] = wingDecoys[2]
    if (wingDecoys.size > 3) ordered[count - 2] = wingDecoys[3]

    var lateIdx = 0
    for (slot in 0 until count) {
        if (ordered[slot] == null) {
            ordered[slot] = lateDecoys.getOrNull(lateIdx)
            lateIdx++
        }
    }

    return BrewDiceRoll(
        deck = ordered.filterNotNull(),
        winnerIndex = winnerSlot,
        winner = winner,
    )
}
