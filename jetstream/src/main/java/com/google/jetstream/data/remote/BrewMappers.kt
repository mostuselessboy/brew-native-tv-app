/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.data.remote

import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.HomeSectionType
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieAward
import com.google.jetstream.data.entities.MovieCast
import com.google.jetstream.data.entities.MovieCategory
import com.google.jetstream.data.entities.MovieCriticReview
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.entities.PurchaseCta
import com.google.jetstream.data.entities.PurchaseCtaSlot
import com.google.jetstream.data.entities.ThumbnailType
import com.google.jetstream.data.util.BrewArtworkUrls
import com.google.jetstream.data.util.BrewDateUtils
import com.google.jetstream.data.util.BrewTrailerUrl
import com.google.jetstream.data.util.CardCommerce
import com.google.jetstream.data.util.DailyRotatingArtwork
import com.google.jetstream.data.util.VodTagBadge

object BrewMappers {

    fun BrewContentDataDto.toMovie(thumbnailType: ThumbnailType = ThumbnailType.Long): Movie? {
        val id = slug?.takeIf { it.isNotBlank() } ?: return null
        val name = projectTitle?.takeIf { it.isNotBlank() } ?: return null

        // Tray / TV cards are always landscape 16:9 (vod-frontend MovieCard default).
        val poster = when (thumbnailType) {
            ThumbnailType.Long -> DailyRotatingArtwork.pickDailyLandscape(
                backgroundArtUrl = backgroundArtUrl,
                horizontalThumbnails = horizontalThumbnails,
                fallback = projectPoster ?: verticalBackgroundArtUrl,
            )
            ThumbnailType.Standard -> DailyRotatingArtwork.pickDailyVertical(
                verticalBackgroundArtUrl = verticalBackgroundArtUrl,
                verticalThumbnails = verticalThumbnails,
                fallback = projectPoster ?: backgroundArtUrl,
            )
        }.takeIf { it.isNotBlank() } ?: return null

        val chrome = CardCommerce.resolve(
            monetizationModel = monetizationModel,
            pricingData = pricingData,
            isSvod = isSvod,
            isTvod = isTvod,
            availableForBuy = availableForBuy,
            availableForRent = availableForRent,
            isStoreContent = isStoreContent,
        )
        val salesPitch = shortDescription?.takeIf { it.isNotBlank() }.orEmpty()
        val durationLabel = runtime?.takeIf { it.isNotBlank() }

        return Movie(
            id = id,
            videoUri = BrewTrailerUrl.toPlayableMp4(trailerOriginalUrl),
            subtitleUri = null,
            posterUri = poster,
            name = name,
            description = salesPitch,
            year = BrewDateUtils.formatReleaseYear(releaseDate),
            country = country?.takeIf { it.isNotBlank() },
            genres = genres,
            duration = durationLabel,
            vodTagLabel = VodTagBadge.movieCardLabel(vodTag),
            isFestivalTag = VodTagBadge.isFestivalStyle(vodTag),
            showStore = chrome.showStore,
            showBrewPlus = chrome.showPlus,
            leavingSoon = BrewDateUtils.isLeaveDateInFuture(leaveDate),
        )
    }

    fun BrewHomeSectionDto.toHomeSection(): HomeSection? {
        // Always landscape art for home trays/carousel — matches brew.tv card shelves.
        val movies = content.mapNotNull { item ->
            val data = item.contentData ?: return@mapNotNull null
            data.toMovie(ThumbnailType.Long)
        }
        if (movies.isEmpty()) return null

        val showRanking = metadata?.showRanking == true ||
            name.contains("Most Watched", ignoreCase = true) ||
            name.contains("Top 10", ignoreCase = true)

        val sectionType = when {
            type == "movie_showcase" -> HomeSectionType.Showcase
            type == "random_movie_picker" -> HomeSectionType.RandomMoviePicker
            type == "movie_tray" && showRanking -> HomeSectionType.Immersive
            type == "movie_tray" -> HomeSectionType.Row
            else -> return null
        }

        return HomeSection(
            id = id.toString(),
            title = name,
            type = sectionType,
            movies = movies,
            subheading = subheading?.takeIf { it.isNotBlank() },
            showRanking = showRanking,
        )
    }

    fun BrewCampaignData.toMovieDetails(
        requestedId: String,
        alsoWatchedMovies: MovieList = emptyList(),
        relatedMovies: MovieList = emptyList(),
    ): MovieDetails {
        val title = projectTitle ?: title ?: project?.projectTitle ?: "Untitled"
        val synopsis = projectSynopsis
            ?: shortDescription
            ?: project?.projectSynopsis
            ?: ""
        val appearance = campaign?.appearance
        val poster = listOfNotNull(
            backgroundArt,
            BrewArtworkUrls.asUrl(appearance?.backgroundArt),
            BrewArtworkUrls.firstUrl(appearance?.horizontalThumbnails),
            verticalBackgroundArt,
            BrewArtworkUrls.asUrl(appearance?.verticalBackgroundArt),
            BrewArtworkUrls.firstUrl(appearance?.verticalThumbnails),
            project?.projectPoster,
        ).firstOrNull { it.isNotBlank() }.orEmpty()
        val videoUri = BrewTrailerUrl.resolveFromCandidates(
            projectTrailer.firstOrNull { it.startsWith("http") && !it.contains("youtube", ignoreCase = true) },
            trailer?.trailerOriginalUrl,
            campaign?.trailerOriginalUrl,
        )

        val runtime = runtimeMinutes?.let { minutes ->
            val hours = minutes / 60
            val mins = minutes % 60
            if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
        } ?: project?.runtime?.let { "$it min" }.orEmpty()

        val release = formatReleaseDate(releaseDate ?: project?.releaseDate)
        val year = BrewDateUtils.formatReleaseYear(releaseDate ?: project?.releaseDate)
        val countrySuffix = country?.takeIf { it.isNotBlank() }.orEmpty()

        val cast = castAndAwards?.castAndCrew.orEmpty().map { member ->
            val displayName = member.fullName?.takeIf { it.isNotBlank() }
                ?: listOfNotNull(member.firstName, member.lastName)
                    .joinToString(" ")
                    .ifBlank { "Unknown" }
            val avatar = member.avatarUrl?.takeIf { it.isNotBlank() }
                ?: member.imageUrl?.takeIf { it.isNotBlank() }
                ?: member.profileImageUrl?.takeIf { it.isNotBlank() }
                ?: ""
            MovieCast(
                id = member.id?.toString() ?: member.slug.orEmpty().ifBlank { displayName },
                characterName = member.characters.firstOrNull()
                    ?: member.job.firstOrNull()?.replace('_', ' ')?.replaceFirstChar { it.uppercase() }
                    ?: "",
                realName = displayName,
                avatarUrl = avatar,
            )
        }

        val awards = castAndAwards?.awards.orEmpty()
            .sortedByDescending { it.anchor }
            .mapNotNull { award ->
                val name = award.name?.takeIf { it.isNotBlank() }
                    ?: award.category?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                MovieAward(
                    name = name,
                    category = award.category.orEmpty(),
                    year = award.year.orEmpty(),
                    logoUrl = award.logo,
                )
            }

        val genericCriticLogo =
            "https://createstir.b-cdn.net/assetlibrary/91/Critic%E2%80%99s%20Review%20Generic%20Logo%20(1).png"
        val mappedCriticReviews = criticReviews
            .mapNotNull { review ->
                val quote = review.review?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val org = review.orgName.orEmpty()
                val author = review.author?.takeIf { it.isNotBlank() } ?: org
                MovieCriticReview(
                    id = review.id?.toString() ?: "${author}_${quote.hashCode()}",
                    quote = quote,
                    author = author,
                    orgName = org,
                    orgLogoUrl = review.orgLogo?.takeIf { it.isNotBlank() && it != genericCriticLogo }
                        ?: genericCriticLogo,
                    dateLabel = formatCriticDate(review.date),
                    link = review.link,
                )
            }
            .sortedBy { review ->
                // Non-generic logos first — same as vod-frontend CriticReviews.tsx
                if (review.orgLogoUrl == genericCriticLogo) 1 else 0
            }

        val director = castAndAwards?.castAndCrew
            ?.firstOrNull { it.job.any { job -> job.equals("director", ignoreCase = true) } }
            ?.fullName
            .orEmpty()
        val screenplay = castAndAwards?.castAndCrew
            ?.firstOrNull { it.job.any { job -> job.equals("writer", ignoreCase = true) } }
            ?.fullName
            .orEmpty()
        val music = castAndAwards?.castAndCrew
            ?.firstOrNull { it.job.any { job -> job.equals("composer", ignoreCase = true) } }
            ?.fullName
            .orEmpty()

        val metaDate = listOfNotNull(year, countrySuffix.takeIf { it.isNotBlank() })
            .joinToString(" • ")
            .ifBlank { release }

        val purchaseCta = purchaseCta?.toPurchaseCta()

        return MovieDetails(
            id = preferredSlug ?: requestedId,
            videoUri = videoUri,
            subtitleUri = null,
            posterUri = poster,
            name = title,
            description = synopsis,
            pgRating = contentRatingLabel?.takeIf { it.isNotBlank() } ?: "NR",
            releaseDate = metaDate,
            categories = genres.ifEmpty { project?.genres.orEmpty() },
            duration = runtime.ifBlank { "—" },
            director = director.ifBlank { "—" },
            screenplay = screenplay.ifBlank { "—" },
            music = music.ifBlank { "—" },
            castAndCrew = cast,
            awards = awards,
            criticReviews = mappedCriticReviews,
            status = campaign?.status?.replaceFirstChar { it.uppercase() } ?: "Released",
            originalLanguage = vodPrimaryLanguage?.name ?: "—",
            budget = "—",
            revenue = rating?.let { String.format("%.1f ★ (%d)", it, ratingCount ?: 0) } ?: "—",
            alsoWatchedMovies = alsoWatchedMovies,
            relatedMovies = relatedMovies,
            purchaseCta = purchaseCta,
            reviewsAndRatings = emptyList(),
        )
    }

    private fun BrewPurchaseCtaDto.toPurchaseCta(): PurchaseCta? {
        if (slots.isEmpty()) return null
        return PurchaseCta(
            scenario = scenario.orEmpty(),
            slots = slots.mapNotNull { slot ->
                val kind = slot.kind?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                PurchaseCtaSlot(
                    kind = kind,
                    color = slot.color?.takeIf { it.isNotBlank() } ?: "white",
                    isContinueWatching = slot.isContinueWatching == true,
                    percentageWatched = slot.percentageWatched ?: 0f,
                    isFree = slot.free == true,
                )
            },
        )
    }

    private fun formatCriticDate(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val instant = java.time.OffsetDateTime.parse(raw)
            instant.format(
                java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.US)
            )
        } catch (_: Exception) {
            raw.take(10)
        }
    }

    fun genresToCategories(movies: List<BrewContentDataDto>): List<MovieCategory> {
        return movies.flatMap { it.genres }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .mapIndexed { index, genre ->
                MovieCategory(id = "genre-$index-$genre", name = genre)
            }
    }

    private fun formatReleaseDate(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.take(10)
    }
}
