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

import com.google.jetstream.data.entities.CollectionSectionDetails
import com.google.jetstream.data.entities.HomeSection
import com.google.jetstream.data.entities.HomeSectionType
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieAward
import com.google.jetstream.data.entities.MovieCast
import com.google.jetstream.data.entities.MovieCategory
import com.google.jetstream.data.entities.MovieSubscriptionPlan
import com.google.jetstream.data.entities.MovieCriticReview
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieEpisodeSeason
import com.google.jetstream.data.entities.MovieExtraItem
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.entities.MovieReviewsAndRatings
import com.google.jetstream.data.entities.MovieReviewSummary
import com.google.jetstream.data.entities.ReviewSection
import com.google.jetstream.data.entities.ThumbnailType
import com.google.jetstream.data.util.BrewArtworkUrls
import com.google.jetstream.data.util.BrewDateUtils
import com.google.jetstream.data.util.BrewTrailerUrl
import com.google.jetstream.data.util.BrewWebUrls
import com.google.jetstream.data.util.CardCommerce
import com.google.jetstream.data.util.ComingSoonUtils
import com.google.jetstream.data.util.DailyRotatingArtwork
import com.google.jetstream.data.util.DetailPurchaseCta
import com.google.jetstream.data.util.MovieLanguageRows
import com.google.jetstream.data.util.RibbonLabel
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
            // Poster: use project_poster directly — the actual portrait movie poster art.
            // Only fall back to vertical/landscape BG art when project_poster is absent.
            ThumbnailType.Poster -> projectPoster?.takeIf { it.isNotBlank() }
                ?: verticalBackgroundArtUrl?.takeIf { it.isNotBlank() }
                ?: backgroundArtUrl.orEmpty()
        }.takeIf { it.isNotBlank() } ?: return null

        val heroBackdrop = if (thumbnailType == ThumbnailType.Long) {
            DailyRotatingArtwork.pickAlternateLandscape(
                backgroundArtUrl = backgroundArtUrl,
                horizontalThumbnails = horizontalThumbnails,
                cardPick = poster,
                fallback = projectPoster ?: backgroundArtUrl,
            ).takeIf { it.isNotBlank() }
        } else {
            null
        }

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
        val viewerReleaseOption = pricingData?.viewerReleaseOption
        val comingSoon = ComingSoonUtils.isComingSoon(distribution, viewerReleaseOption)

        return Movie(
            id = id,
            videoUri = BrewTrailerUrl.toPlayableMp4(trailerOriginalUrl),
            subtitleUri = null,
            posterUri = poster,
            heroBackdropUri = heroBackdrop,
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
            ribbonLabel = RibbonLabel.resolve(ribbonLabel, projectType),
            projectType = projectType,
            isComingSoon = comingSoon,
            isFreeTier = isFreeTier == true,
            rentPriceFormatted = rentPriceFormatted?.takeIf { it.isNotBlank() },
            comingSoonHint = if (comingSoon) {
                ComingSoonUtils.releaseHint(comingSoonReleaseInfo, releaseDate)
            } else {
                null
            },
            campaignId = this.id,
            campaignVersionId = campaignVersionId,
        )
    }

    /** Customers-also-watched / related-movies tray cards — same meta fields as home cards. */
    fun BrewAlsoWatchedMovieDto.toMovie(): Movie? {
        val id = cvName?.takeIf { it.isNotBlank() } ?: return null
        val name = projectTitle?.takeIf { it.isNotBlank() } ?: return null
        val backgroundArt = BrewArtworkUrls.asUrl(appearance?.get("background_art"))
        val horizontalThumbnails = BrewArtworkUrls.horizontalAlternatesFromAppearance(appearance)
        val posterFallback = projectPoster?.takeIf { it.isNotBlank() }
            ?: BrewArtworkUrls.asUrl(appearance?.get("poster"))
        val landscape = DailyRotatingArtwork.pickDailyLandscape(
            backgroundArtUrl = backgroundArt,
            horizontalThumbnails = horizontalThumbnails,
            fallback = posterFallback ?: BrewArtworkUrls.landscapeFromAppearance(appearance),
        ).takeIf { it.isNotBlank() }
            ?: posterFallback?.takeIf { it.isNotBlank() }
            ?: return null
        val chrome = CardCommerce.resolve(
            monetizationModel = monetizationModel,
            pricingData = null,
            isSvod = isSvod,
            isTvod = isTvod,
            availableForBuy = availableForBuy,
            availableForRent = availableForRent,
            isStoreContent = isStoreContent,
        )
        return Movie(
            id = id,
            videoUri = "",
            subtitleUri = null,
            posterUri = landscape,
            name = name,
            description = shortDescription?.takeIf { it.isNotBlank() }.orEmpty(),
            year = BrewDateUtils.formatReleaseYear(releaseDate),
            country = country?.takeIf { it.isNotBlank() },
            genres = genres,
            vodTagLabel = VodTagBadge.movieCardLabel(vodTag),
            isFestivalTag = VodTagBadge.isFestivalStyle(vodTag),
            showStore = chrome.showStore,
            showBrewPlus = chrome.showPlus,
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
            showRanking = showRanking,
            subheading = subheading?.takeIf { it.isNotBlank() },
            slug = slug?.takeIf { it.isNotBlank() } ?: id.toString(),
        )
    }

    fun BrewHomeSectionDto.toCollectionSectionDetails(): CollectionSectionDetails? {
        val movies = content.mapNotNull { item ->
            val data = item.contentData ?: return@mapNotNull null
            data.toMovie(ThumbnailType.Long)
        }
        if (movies.isEmpty()) return null

        return CollectionSectionDetails(
            id = id.toString(),
            slug = slug?.takeIf { it.isNotBlank() } ?: id.toString(),
            title = name,
            subheading = subheading?.takeIf { it.isNotBlank() },
            movies = movies,
            total = total ?: movies.size,
            heroPosterUri = movies.firstOrNull()?.posterUri,
        )
    }

    fun BrewCampaignData.toMovieDetails(
        requestedId: String,
        customersAlsoWatched: MovieList = emptyList(),
        relatedMovies: MovieList = emptyList(),
        userReviewComments: List<MovieReviewsAndRatings> = emptyList(),
        reviewSummary: MovieReviewSummary? = null,
        userCountry: String = "",
        catalogOverlay: BrewContentDataDto? = null,
    ): MovieDetails {
        val title = projectTitle ?: title ?: project?.projectTitle ?: "Untitled"
        val synopsis = projectSynopsis
            ?: shortDescription
            ?: project?.projectSynopsis
            ?: ""
        val appearance = campaign?.appearance
        val backgroundArtUrl = backgroundArt ?: BrewArtworkUrls.asUrl(appearance?.backgroundArt)
        val horizontalThumbnails = BrewArtworkUrls.collectUrlList(appearance?.horizontalThumbnails)
        val fallback = listOfNotNull(
            verticalBackgroundArt,
            BrewArtworkUrls.asUrl(appearance?.verticalBackgroundArt),
            BrewArtworkUrls.firstUrl(appearance?.verticalThumbnails),
            project?.projectPoster,
        ).firstOrNull { it.isNotBlank() }.orEmpty()

        val poster = DailyRotatingArtwork.pickDailyLandscape(
            backgroundArtUrl = backgroundArtUrl,
            horizontalThumbnails = horizontalThumbnails,
            fallback = fallback,
        ).ifBlank { backgroundArtUrl ?: fallback }

        val portraitPoster = listOfNotNull(
            project?.projectPoster,
            verticalBackgroundArt,
            BrewArtworkUrls.asUrl(appearance?.verticalBackgroundArt),
            BrewArtworkUrls.firstUrl(appearance?.verticalThumbnails),
        ).firstOrNull { it.isNotBlank() }?.takeIf { it.isNotBlank() } ?: fallback

        val videoUri = "" // Trailers use DRM HLS via trailer vod asset — not MP4 fallback.
        val trailerVodAssetId = trailer?.vodAssetId?.takeIf { it > 0 }
        val trailerIsDrm = trailer?.isDrm == true
        val trailerOriginalUrl = listOfNotNull(
            trailer?.trailerOriginalUrl?.takeIf { it.isNotBlank() },
            projectTrailer.firstOrNull { it.isNotBlank() },
        ).firstOrNull()
        val trailerIsYoutube = BrewWebUrls.isYoutube(trailerOriginalUrl)
        val trailerIsPublic = trailer?.isPublic == true
        val resolvedRibbonLabel = RibbonLabel.resolve(
            ribbonLabel ?: campaign?.ribbonLabel ?: project?.ribbonLabel,
            projectType,
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
                id = member.id?.let { mid ->
                    val slug = member.slug.orEmpty()
                    if (slug.startsWith("$mid-")) slug
                    else if (slug.isNotBlank()) "$mid-$slug"
                    else mid.toString()
                } ?: member.slug.orEmpty().ifBlank { displayName },
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

        // Keep slug-backed movie ids — numeric-only ids are not navigable slugs.
        fun isNavigableMovieId(id: String): Boolean =
            id.isNotBlank() && !id.all { it.isDigit() }

        val alsoWatched = customersAlsoWatched
            .filter { isNavigableMovieId(it.id) }
            .take(12)
        val related = relatedMovies
            .filter { isNavigableMovieId(it.id) }
            .filter { movie -> alsoWatched.none { it.id == movie.id } }
            .take(12)

        val userReviewCards = userReviewComments.ifEmpty {
            userReviews.mapNotNull { review ->
                val name = review.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val body = review.review?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                MovieReviewsAndRatings(
                    reviewerName = name,
                    reviewerIconUri = "",
                    reviewBody = body,
                    reviewRating = review.rating,
                )
            }
        }

        val metaDate = listOfNotNull(year, countrySuffix.takeIf { it.isNotBlank() })
            .joinToString(" • ")
            .ifBlank { release }

        val overlayPricing = catalogOverlay?.pricingData ?: pricingData
        val pricingModels = overlayPricing?.viewerMonetizationModels?.keys
            ?.map { it.lowercase() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val monetization = when {
            monetizationModel.isNotEmpty() -> monetizationModel
            catalogOverlay?.monetizationModel?.isNotEmpty() == true -> catalogOverlay.monetizationModel
            pricingModels.isNotEmpty() -> pricingModels
            else -> emptyList()
        }
        val chrome = CardCommerce.resolve(
            monetizationModel = monetization,
            pricingData = overlayPricing,
            isSvod = isSvod ?: catalogOverlay?.isSvod,
            isTvod = isTvod ?: catalogOverlay?.isTvod,
            availableForBuy = availableForBuy ?: catalogOverlay?.availableForBuy,
            availableForRent = availableForRent ?: catalogOverlay?.availableForRent,
            isStoreContent = isStoreContent ?: catalogOverlay?.isStoreContent,
        )
        val distributionValue = distribution
            ?: campaign?.distribution
            ?: catalogOverlay?.distribution
        val viewerReleaseOption = overlayPricing?.viewerReleaseOption
        val comingSoon = ComingSoonUtils.isComingSoon(distributionValue, viewerReleaseOption)
        val rentFormatted = rentPriceFormatted?.takeIf { it.isNotBlank() }
            ?: catalogOverlay?.rentPriceFormatted?.takeIf { it.isNotBlank() }
            ?: overlayPricing?.rent?.firstOrNull()?.let { formatPrice(it) }
        val buyFormatted = overlayPricing?.buy?.firstOrNull()?.let { formatPrice(it) }
        val buyOriginal = overlayPricing?.buy?.firstOrNull()?.perceivedPrice?.let { perceived ->
            val symbol = overlayPricing.buy.firstOrNull()?.currencySymbol ?: "₹"
            "$symbol${perceived.toInt()}"
        }
        val rentOriginal = overlayPricing?.rent?.firstOrNull()?.perceivedPrice?.let { perceived ->
            val symbol = overlayPricing.rent.firstOrNull()?.currencySymbol ?: "₹"
            "$symbol${perceived.toInt()}"
        }
        val tagline = shortDescription?.takeIf { it.isNotBlank() }.orEmpty()
        val languageRows = MovieLanguageRows.build(vodPrimaryLanguage, subtitles)
        val resolvedProjectType = projectType ?: catalogOverlay?.projectType
        val freeTier = isFreeTier == true || catalogOverlay?.isFreeTier == true

        val resolvedImdbLink = imdbUrl?.takeIf { it.isNotBlank() }
            ?: project?.imdbLink?.takeIf { it.isNotBlank() }
            .orEmpty()
        val resolvedLetterboxdLink = letterboxdUrl?.takeIf { it.isNotBlank() }
            ?: letterboxdLink?.takeIf { it.isNotBlank() }
            ?: project?.letterboxdLink?.takeIf { it.isNotBlank() }
            .orEmpty()
        val resolvedRottenTomatoesLink = rottenTomatoesLink?.takeIf { it.isNotBlank() }
            ?: project?.rottenTomatoesLink?.takeIf { it.isNotBlank() }
            .orEmpty()

        val mappedSubscriptionPlans = subscriptionPlans.map { it.toMovieSubscriptionPlan() }
        val purchaseCtaSlots = DetailPurchaseCta.mapFromApi(
            purchaseCta = purchaseCta,
            rentPriceFormatted = rentFormatted,
            buyPriceFormatted = buyFormatted,
            rentOriginalPriceFormatted = rentOriginal,
            buyOriginalPriceFormatted = buyOriginal,
            subscriptionPlans = mappedSubscriptionPlans,
            comingSoonHint = if (comingSoon) {
                ComingSoonUtils.releaseHint(
                    comingSoonReleaseInfo ?: catalogOverlay?.comingSoonReleaseInfo,
                    releaseDate ?: project?.releaseDate,
                )
            } else {
                null
            },
        )
        val resolvedCvName = campaign?.cvName?.takeIf { it.isNotBlank() }
            ?: preferredSlug?.takeIf { it.isNotBlank() }
            ?: requestedId

        return MovieDetails(
            id = preferredSlug ?: requestedId,
            videoUri = videoUri,
            subtitleUri = null,
            posterUri = poster,
            portraitPosterUri = portraitPoster,
            name = title,
            description = synopsis,
            tagline = tagline,
            pgRating = contentRatingLabel?.takeIf { it.isNotBlank() } ?: "NR",
            releaseDate = metaDate,
            categories = genres.ifEmpty { project?.genres.orEmpty() },
            duration = runtime.ifBlank { "—" },
            releaseYear = year.orEmpty(),
            imdbLink = resolvedImdbLink,
            letterboxdLink = resolvedLetterboxdLink,
            rottenTomatoesLink = resolvedRottenTomatoesLink,
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
            averageRating = rating,
            ratingCount = ratingCount,
            customersAlsoWatched = alsoWatched,
            relatedMovies = related,
            reviewsAndRatings = userReviewCards,
            reviewSummary = reviewSummary,
            userCountry = userCountry,
            projectType = resolvedProjectType,
            isComingSoon = comingSoon,
            comingSoonHint = if (comingSoon) {
                ComingSoonUtils.releaseHint(comingSoonReleaseInfo ?: catalogOverlay?.comingSoonReleaseInfo, releaseDate ?: project?.releaseDate)
            } else {
                null
            },
            isFreeTier = freeTier && !chrome.showPlus && !chrome.showStore,
            showBrewPlus = chrome.showPlus,
            showStore = chrome.showStore,
            rentPriceFormatted = rentFormatted,
            buyPriceFormatted = buyFormatted,
            rentOriginalPriceFormatted = rentOriginal,
            buyOriginalPriceFormatted = buyOriginal,
            languageRows = languageRows,
            hasTrailer = trailerIsYoutube || trailerVodAssetId != null,
            trailerVodAssetId = trailerVodAssetId,
            trailerIsDrm = trailerIsDrm,
            trailerOriginalUrl = trailerOriginalUrl,
            trailerIsYoutube = trailerIsYoutube,
            trailerIsPublic = trailerIsPublic,
            subscriptionPlans = mappedSubscriptionPlans,
            ribbonLabel = resolvedRibbonLabel,
            purchaseCtaSlots = purchaseCtaSlots,
            vodAssetId = resolveVodAssetId(),
            cvName = resolvedCvName,
            campaignVersionId = campaign?.campaignVersionId,
            campaignId = campaign?.id?.takeIf { it > 0 },
            bonusClips = mapBonusClips(),
            episodeSeasons = mapEpisodeSeasons(),
        )
    }

    private fun BrewCampaignData.mapBonusClips(): List<MovieExtraItem> {
        return resolvedBonusClips().mapNotNull { clip ->
            val title = clip.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            MovieExtraItem(
                id = "bonus_${clip.vodAssetId ?: title.hashCode()}",
                title = title,
                thumbnailUri = clip.thumbnail?.takeIf { it.isNotBlank() }.orEmpty(),
                vodAssetId = clip.vodAssetId?.takeIf { it > 0 },
                subtitle = formatExtraDuration(clip.duration),
            )
        }
    }

    private fun BrewCampaignData.mapEpisodeSeasons(): List<MovieEpisodeSeason> {
        val seasonMeta = resolvedVodSeriesMetadata()?.seasons.orEmpty()
        return resolvedSeriesData().mapNotNull { season ->
            val seasonNo = season.seasonNo ?: return@mapNotNull null
            val meta = seasonMeta.firstOrNull { it.index == seasonNo }
            val seasonTitle = meta?.name?.takeIf { it.isNotBlank() } ?: "Season $seasonNo"
            val episodes = season.episodes.mapNotNull { episode ->
                val epTitle = episode.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val epNo = episode.episodeNo
                MovieExtraItem(
                    id = "ep_${episode.vodAssetId ?: "${seasonNo}_$epNo"}",
                    title = epTitle,
                    thumbnailUri = episode.thumbnail?.takeIf { it.isNotBlank() }.orEmpty(),
                    vodAssetId = episode.vodAssetId?.takeIf { it > 0 },
                    subtitle = epNo?.takeIf { it > 0 }?.let { "E$it" }
                        ?: formatExtraDuration(episode.duration),
                )
            }
            if (episodes.isEmpty()) return@mapNotNull null
            MovieEpisodeSeason(seasonNo = seasonNo, title = seasonTitle, episodes = episodes)
        }
    }

    private fun formatExtraDuration(seconds: Int?): String? {
        val total = seconds ?: return null
        if (total <= 0) return null
        val mins = total / 60
        val secs = total % 60
        return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    private fun BrewCampaignData.resolveVodAssetId(): Int? {
        resolvedMovieDetails()?.vodAssetId?.takeIf { it > 0 }?.let { return it }
        resolvedMovieDetails()?.movieAssetId?.takeIf { it > 0 }?.let { return it }
        vodAssetId?.takeIf { it > 0 }?.let { return it }
        campaign?.vodAssetId?.takeIf { it > 0 }?.let { return it }
        project?.vodAssetId?.takeIf { it > 0 }?.let { return it }
        return null
    }

    private fun BrewCampaignData.resolvedBonusClips(): List<BrewBonusClipDto> =
        bonusClips.ifEmpty { campaign?.bonusClips.orEmpty() }

    private fun BrewCampaignData.resolvedSeriesData(): List<BrewSeriesSeasonDto> =
        seriesData.ifEmpty { campaign?.seriesData.orEmpty() }

    private fun BrewCampaignData.resolvedVodSeriesMetadata(): BrewVodSeriesMetadataDto? =
        vodSeriesMetadata ?: campaign?.vodSeriesMetadata

    private fun BrewCampaignData.resolvedMovieDetails(): BrewMovieDetailsDto? =
        movieDetails ?: campaign?.movieDetails

    private fun formatPrice(option: BrewPricingOptionDto): String {
        val symbol = option.currencySymbol?.takeIf { it.isNotBlank() } ?: "₹"
        val amount = option.price?.toInt() ?: return symbol
        return "$symbol$amount"
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

    fun BrewSubscriptionPlanDto.toMovieSubscriptionPlan(): MovieSubscriptionPlan =
        MovieSubscriptionPlan(
            id = id,
            name = name,
            price = price,
            perceivedPrice = perceivedPrice,
            currencySymbol = currencySymbol?.takeIf { it.isNotBlank() }
                ?: currency?.let(::subscriptionCurrencySymbol),
            intervalUnit = intervalUnit,
            intervalCount = intervalCount,
            isActive = isActive,
        )

    private fun subscriptionCurrencySymbol(currency: String): String =
        when (currency.trim().uppercase()) {
            "INR" -> "₹"
            "USD" -> "$"
            else -> currency
        }

    fun BrewCommentDto.toUserReview(): MovieReviewsAndRatings? {
        val name = user?.name?.takeIf { it.isNotBlank() } ?: return null
        val body = text?.takeIf { it.isNotBlank() }
            ?: heading?.takeIf { it.isNotBlank() }
            ?: return null
        val rating5 = starRating?.takeIf { it > 0 }?.let { it / 2.0 }
        return MovieReviewsAndRatings(
            id = id.orEmpty(),
            reviewerName = name,
            reviewerUsername = user?.username.orEmpty(),
            reviewerIconUri = user?.image.orEmpty(),
            reviewHeading = heading.orEmpty(),
            reviewBody = body,
            reviewRating = rating5,
            createdAt = createdAt.orEmpty(),
            countryCode = user?.countryCode.orEmpty(),
            countryName = user?.countryName.orEmpty(),
            isVerifiedCritic = user?.isBrewCritic == true,
            section = ReviewSection.fromApi(section),
        )
    }

    fun BrewCommentsData.toReviewSummary(): MovieReviewSummary? {
        val total = totalRatings ?: return null
        if (total <= 0) return null
        val distribution = ratingDistribution.mapKeys { (key, _) -> key.toIntOrNull() ?: 0 }
            .filterKeys { it in 1..10 }
        val avg5 = averageRating?.takeIf { it > 0 }?.let { it / 2.0 } ?: 0.0
        return MovieReviewSummary(
            averageRating = avg5,
            totalRatings = total,
            ratingDistribution = distribution,
        )
    }
}
