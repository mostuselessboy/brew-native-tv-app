package com.google.jetstream.data.playback

import com.google.jetstream.data.auth.AuthSessionStore
import com.google.jetstream.data.entities.EndScreenAction
import com.google.jetstream.data.entities.EndScreenRecommendation
import com.google.jetstream.data.util.LibraryClickAction
import com.google.jetstream.data.entities.LibraryItem
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.PlaybackRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Prepares VOD playback and stores intent before navigating to the player. */
@Singleton
class PlaybackLauncher @Inject constructor(
    private val movieRepository: MovieRepository,
    private val playbackRepository: PlaybackRepository,
    private val playbackIntentStore: PlaybackIntentStore,
    private val authSessionStore: AuthSessionStore,
) {

    suspend fun launchMovie(movie: Movie): Result<String> {
        val userId = authSessionStore.currentUserId()
            ?: return Result.failure(IllegalStateException("Sign in to watch"))

        if (
            movie.libraryClickAction == LibraryClickAction.Play &&
            movie.vodAssetId != null &&
            movie.vodAssetId > 0
        ) {
            return prepareAndStore(
                slug = movie.id,
                intentResult = playbackRepository.prepareDirectPlayback(
                    userId = userId,
                    cvName = movie.id,
                    vodAssetId = movie.vodAssetId,
                    title = movie.name,
                    initialTimeMs = (movie.initialTimeSeconds ?: 0) * 1000L,
                ),
            )
        }

        return launchFeatureBySlug(movie.id, userId)
    }

    suspend fun launchLibraryItem(item: LibraryItem): Result<String> {
        if (item.clickAction == LibraryClickAction.Nothing) {
            return Result.failure(IllegalStateException("Unavailable"))
        }
        if (item.clickAction == LibraryClickAction.OpenMoviePage) {
            return Result.failure(IllegalStateException("Open detail"))
        }

        val userId = authSessionStore.currentUserId()
            ?: return Result.failure(IllegalStateException("Sign in to watch"))

        val vodAssetId = item.vodAssetId?.takeIf { it > 0 }
            ?: return launchFeatureBySlug(item.movieId, userId)

        return prepareAndStore(
            slug = item.movieId,
            intentResult = playbackRepository.prepareDirectPlayback(
                userId = userId,
                cvName = item.movieId,
                vodAssetId = vodAssetId,
                title = item.movie.name,
                initialTimeMs = item.initialTimeSeconds * 1000L,
            ),
        )
    }

    suspend fun launchEndScreenPick(pick: EndScreenRecommendation): Result<String> {
        val userId = authSessionStore.currentUserId()
        return when (pick.action) {
            EndScreenAction.Detail -> Result.failure(IllegalStateException("Open detail"))
            EndScreenAction.Trailer -> {
                val uid = userId ?: return Result.failure(IllegalStateException("Sign in to watch"))
                val details = runCatching { movieRepository.getMovieDetails(pick.slug) }
                    .getOrElse { return Result.failure(it) }
                val intent = playbackRepository.prepareTrailerPlayback(details, uid)
                    ?: return Result.failure(IllegalStateException("No trailer available"))
                playbackIntentStore.set(intent)
                Result.success(pick.slug)
            }
            EndScreenAction.Player -> {
                if (userId == null) {
                    return Result.failure(IllegalStateException("Sign in to watch"))
                }
                val assetId = pick.vodAssetId?.takeIf { it > 0 }
                if (assetId != null) {
                    prepareAndStore(
                        slug = pick.slug,
                        intentResult = playbackRepository.prepareDirectPlayback(
                            userId = userId,
                            cvName = pick.slug,
                            vodAssetId = assetId,
                            title = pick.title,
                        ),
                    )
                } else {
                    launchFeatureBySlug(pick.slug, userId)
                }
            }
        }
    }

    private suspend fun launchFeatureBySlug(slug: String, userId: Int): Result<String> {
        val details = runCatching { movieRepository.getMovieDetails(slug) }
            .getOrElse { return Result.failure(it) }
        val cvName = details.cvName.ifBlank { details.id }
        val checkPurchase = playbackRepository.checkPurchase(
            userId = userId,
            cvName = cvName,
            campaignVersionId = details.campaignVersionId,
        )
        return prepareAndStore(
            slug = details.id,
            intentResult = playbackRepository.prepareFeaturePlayback(
                movie = details,
                checkPurchase = checkPurchase,
                userId = userId,
            ),
        )
    }

    private fun prepareAndStore(
        slug: String,
        intentResult: Result<PlaybackIntent>,
    ): Result<String> = intentResult.map { intent ->
        playbackIntentStore.set(intent)
        slug
    }
}
