package com.google.jetstream.data.util

import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.remote.BrewPurchaseCtaDto
import com.google.jetstream.data.remote.BrewPurchaseCtaSlotDto
import com.google.jetstream.data.remote.BrewSubscriptionPlanDto
import java.util.Locale

/** Mirrors mobile-viewer `purchase-ctas/tokens` + slot kinds. */
enum class DetailCtaColor { Yellow, White }

enum class DetailCtaKind {
    WatchForFree,
    WatchNow,
    ContinueWatching,
    ComingSoon,
    ComingSoonNotify,
    Rent,
    Buy,
    SubscribeYearly,
    SubscribeQuarterly,
    SupportFilmmaker,
    NotAvailable,
}

data class DetailPurchaseCtaSlot(
    val kind: DetailCtaKind,
    val color: DetailCtaColor,
    val title: String,
    val sublabel: String? = null,
    val price: String? = null,
    val originalPrice: String? = null,
    val intervalSuffix: String? = null,
    val showBrewPlusLogo: Boolean = false,
    val progressPercent: Int = 0,
)

/** Purchase stack — API `purchase_cta.slots` when present, else client fallback. */
object DetailPurchaseCta {

    private const val DEFAULT_RENT_SUBLABEL = "520+ rented this week"
    private const val SUBSCRIBE_SUBLABEL = "One year. Full Catalog. No Autopay"
    private const val FREE_SUBLABEL = "No Ads, Quick sign-up"

    fun slots(movie: MovieDetails): List<DetailPurchaseCtaSlot> {
        if (movie.purchaseCtaSlots.isNotEmpty()) return movie.purchaseCtaSlots
        return legacySlots(movie)
    }

    fun primaryRowSlots(movie: MovieDetails): List<DetailPurchaseCtaSlot> =
        slots(movie).take(2)

    fun mapFromApi(
        purchaseCta: BrewPurchaseCtaDto?,
        rentPriceFormatted: String?,
        buyPriceFormatted: String?,
        rentOriginalPriceFormatted: String?,
        subscriptionPlans: List<BrewSubscriptionPlanDto> = emptyList(),
        comingSoonHint: String?,
    ): List<DetailPurchaseCtaSlot> {
        val apiSlots = purchaseCta?.slots.orEmpty()
        if (apiSlots.isEmpty()) return emptyList()
        val yearlyPlan = pickYearlySubscriptionPlan(subscriptionPlans)
        val quarterlyPlan = pickQuarterlySubscriptionPlan(subscriptionPlans)
        return apiSlots.mapNotNull { dto ->
            mapApiSlot(
                dto = dto,
                rentPriceFormatted = rentPriceFormatted,
                buyPriceFormatted = buyPriceFormatted,
                rentOriginalPriceFormatted = rentOriginalPriceFormatted,
                yearlyPlan = yearlyPlan,
                quarterlyPlan = quarterlyPlan,
                comingSoonHint = comingSoonHint,
            )
        }
    }

    private fun mapApiSlot(
        dto: BrewPurchaseCtaSlotDto,
        rentPriceFormatted: String?,
        buyPriceFormatted: String?,
        rentOriginalPriceFormatted: String?,
        yearlyPlan: BrewSubscriptionPlanDto?,
        quarterlyPlan: BrewSubscriptionPlanDto?,
        comingSoonHint: String?,
    ): DetailPurchaseCtaSlot? {
        if (dto.kind == "not_available") return null
        val kind = mapApiKind(dto.kind) ?: return null
        val color = when (dto.color?.lowercase()) {
            "yellow" -> DetailCtaColor.Yellow
            else -> DetailCtaColor.White
        }
        val copy = slotCopy(
            apiKind = dto.kind,
            kind = kind,
            free = dto.free == true,
            continueWatching = dto.isContinueWatching == true,
            comingSoonHint = comingSoonHint,
        )
        return DetailPurchaseCtaSlot(
            kind = if (dto.kind == "watch" && dto.free == true) {
                DetailCtaKind.WatchForFree
            } else {
                kind
            },
            color = color,
            title = copy.title,
            sublabel = copy.sublabel,
            price = when (kind) {
                DetailCtaKind.Rent -> rentPriceFormatted?.takeIf { it.isNotBlank() }
                DetailCtaKind.Buy -> buyPriceFormatted?.takeIf { it.isNotBlank() }
                DetailCtaKind.SubscribeYearly ->
                    yearlyPlan?.let { formatSubscriptionPrice(it) } ?: "₹999"
                DetailCtaKind.SubscribeQuarterly ->
                    quarterlyPlan?.let { formatSubscriptionPrice(it) } ?: "₹999"
                else -> null
            },
            originalPrice = when (kind) {
                DetailCtaKind.Rent -> rentOriginalPriceFormatted?.takeIf { it.isNotBlank() }
                DetailCtaKind.SubscribeYearly -> formatSubscriptionOriginalPrice(yearlyPlan)
                DetailCtaKind.SubscribeQuarterly -> formatSubscriptionOriginalPrice(quarterlyPlan)
                else -> null
            },
            intervalSuffix = when (kind) {
                DetailCtaKind.SubscribeYearly -> "/yr"
                DetailCtaKind.SubscribeQuarterly -> "/qtr"
                else -> null
            },
            showBrewPlusLogo = kind == DetailCtaKind.SubscribeYearly,
            progressPercent = dto.percentageWatched ?: 0,
        )
    }

    private data class SlotCopy(val title: String, val sublabel: String?)

    /** Mirrors mobile-viewer `watchSlotCopy` + `slotStaticCopy`. */
    private fun slotCopy(
        apiKind: String?,
        kind: DetailCtaKind,
        free: Boolean,
        continueWatching: Boolean,
        comingSoonHint: String?,
    ): SlotCopy = when {
        apiKind == "watch" -> watchSlotCopy(free, continueWatching)
        kind == DetailCtaKind.SupportFilmmaker -> SlotCopy(
            title = "Support the filmmaker",
            sublabel = "Fund their craft",
        )
        kind == DetailCtaKind.WatchForFree -> watchSlotCopy(free = true, continueWatching)
        kind == DetailCtaKind.WatchNow || kind == DetailCtaKind.ContinueWatching ->
            watchSlotCopy(free = false, continueWatching)
        kind == DetailCtaKind.Rent -> SlotCopy("Rent movie", DEFAULT_RENT_SUBLABEL)
        kind == DetailCtaKind.Buy -> SlotCopy("Buy movie", "180+ bought this week")
        kind == DetailCtaKind.SubscribeYearly -> SlotCopy("Watch with", SUBSCRIBE_SUBLABEL)
        kind == DetailCtaKind.SubscribeQuarterly ->
            SlotCopy("Quarterly Plan", "Unlock full catalog for 3 months")
        kind == DetailCtaKind.ComingSoon || kind == DetailCtaKind.ComingSoonNotify -> SlotCopy(
            title = "Remind me",
            sublabel = comingSoonHint ?: "Coming soon",
        )
        else -> SlotCopy(kind.name, null)
    }

    private fun watchSlotCopy(free: Boolean, continueWatching: Boolean): SlotCopy {
        if (free) {
            return SlotCopy(
                title = if (continueWatching) "Continue Watching" else "Watch for free",
                sublabel = FREE_SUBLABEL,
            )
        }
        if (continueWatching) {
            return SlotCopy("Continue Watching", "Included with Brew+")
        }
        return SlotCopy("Watch Now", "Included with Brew+")
    }

    private fun mapApiKind(raw: String?): DetailCtaKind? = when (raw) {
        "watch" -> DetailCtaKind.WatchNow
        "rent" -> DetailCtaKind.Rent
        "buy" -> DetailCtaKind.Buy
        "subscribe-yearly" -> DetailCtaKind.SubscribeYearly
        "subscribe-quarterly" -> DetailCtaKind.SubscribeQuarterly
        "coming-soon" -> DetailCtaKind.ComingSoonNotify
        "support_filmmaker" -> DetailCtaKind.SupportFilmmaker
        "coin" -> DetailCtaKind.WatchNow
        else -> null
    }

    private fun legacySlots(movie: MovieDetails): List<DetailPurchaseCtaSlot> {
        if (movie.isComingSoon) {
            return listOf(
                DetailPurchaseCtaSlot(
                    kind = DetailCtaKind.ComingSoonNotify,
                    color = DetailCtaColor.White,
                    title = "Remind me",
                    sublabel = movie.comingSoonHint ?: "Coming soon",
                ),
            )
        }

        val projectType = movie.projectType?.lowercase()?.trim().orEmpty()
        val isShortFilm = projectType == "short-film"

        if (isShortFilm || movie.isFreeTier) {
            return listOf(
                DetailPurchaseCtaSlot(
                    kind = DetailCtaKind.WatchForFree,
                    color = DetailCtaColor.Yellow,
                    title = "Watch for free",
                    sublabel = FREE_SUBLABEL,
                ),
            )
        }

        val hasSvod = movie.showBrewPlus
        val hasTvod = movie.showStore

        return when {
            hasSvod && hasTvod -> listOf(rentSlot(movie), subscribeSlot())
            hasSvod && !hasTvod -> listOf(subscribeSlot())
            hasTvod && !hasSvod -> listOf(rentSlot(movie), subscribeSlot())
            else -> listOf(
                DetailPurchaseCtaSlot(
                    kind = DetailCtaKind.WatchNow,
                    color = DetailCtaColor.Yellow,
                    title = "Watch Now",
                    sublabel = "Included with Brew+",
                ),
            )
        }
    }

    private fun rentSlot(movie: MovieDetails): DetailPurchaseCtaSlot {
        val price = movie.rentPriceFormatted?.takeIf { it.isNotBlank() } ?: "Rent"
        return DetailPurchaseCtaSlot(
            kind = DetailCtaKind.Rent,
            color = DetailCtaColor.Yellow,
            title = "Rent movie",
            sublabel = DEFAULT_RENT_SUBLABEL,
            price = price,
            originalPrice = movie.rentOriginalPriceFormatted?.takeIf { it.isNotBlank() },
        )
    }

    private fun subscribeSlot(): DetailPurchaseCtaSlot =
        DetailPurchaseCtaSlot(
            kind = DetailCtaKind.SubscribeYearly,
            color = DetailCtaColor.White,
            title = "Watch with",
            sublabel = SUBSCRIBE_SUBLABEL,
            price = "₹999",
            originalPrice = "₹1,499",
            intervalSuffix = "/yr",
            showBrewPlusLogo = true,
        )

    private fun pickYearlySubscriptionPlan(
        plans: List<BrewSubscriptionPlanDto>,
    ): BrewSubscriptionPlanDto? =
        activeSubscriptionPlans(plans).firstOrNull { plan ->
            val unit = plan.intervalUnit?.lowercase().orEmpty()
            unit.startsWith("year") || unit == "annual" || unit == "annually" ||
                plan.name.orEmpty().contains(Regex("annual|yearly|\\byear\\b", RegexOption.IGNORE_CASE))
        }

    private fun pickQuarterlySubscriptionPlan(
        plans: List<BrewSubscriptionPlanDto>,
    ): BrewSubscriptionPlanDto? =
        activeSubscriptionPlans(plans).firstOrNull { plan ->
            val unit = plan.intervalUnit?.lowercase().orEmpty()
            val count = plan.intervalCount ?: 0
            unit.startsWith("quarter") || unit == "3_month" || unit == "3_months" ||
                ((unit == "monthly" || unit == "month" || unit.startsWith("month")) && count == 3) ||
                plan.name.orEmpty().contains(Regex("quarter", RegexOption.IGNORE_CASE))
        }

    private fun activeSubscriptionPlans(
        plans: List<BrewSubscriptionPlanDto>,
    ): List<BrewSubscriptionPlanDto> {
        val seen = mutableSetOf<Int>()
        return plans.filter { plan ->
            plan.isActive && plan.price > 0 && seen.add(plan.id)
        }
    }

    private fun formatSubscriptionPrice(plan: BrewSubscriptionPlanDto): String {
        val symbol = plan.currencySymbol?.takeIf { it.isNotBlank() } ?: "₹"
        return "$symbol${formatSubscriptionAmount(plan.price)}"
    }

    private fun formatSubscriptionOriginalPrice(plan: BrewSubscriptionPlanDto?): String? {
        if (plan == null) return null
        val perceived = plan.perceivedPrice ?: return null
        if (perceived <= plan.price) return null
        val symbol = plan.currencySymbol?.takeIf { it.isNotBlank() } ?: "₹"
        return "$symbol${formatSubscriptionAmount(perceived)}"
    }

    private fun formatSubscriptionAmount(value: Double): String {
        val formatter = java.text.NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = if (value % 1.0 == 0.0) 0 else 2
            minimumFractionDigits = 0
        }
        return formatter.format(value)
    }
}
