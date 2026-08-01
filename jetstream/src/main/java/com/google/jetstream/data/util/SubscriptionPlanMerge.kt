package com.google.jetstream.data.util

import com.google.jetstream.data.entities.MovieSubscriptionPlan

/** Merges subscription plan lists — mirrors mobile `mergeSubscriptionPlanSources`. */
object SubscriptionPlanMerge {

    fun merge(vararg sources: List<MovieSubscriptionPlan>): List<MovieSubscriptionPlan> {
        val byId = mutableMapOf<Int, MovieSubscriptionPlan>()
        for (source in sources) {
            for (plan in source) {
                if (!plan.isActive || plan.price <= 0) continue
                val existing = byId[plan.id]
                byId[plan.id] = when {
                    existing == null -> plan
                    existing.isActive && !plan.isActive -> existing
                    !existing.isActive && plan.isActive -> plan
                    else -> mergeFields(existing, plan)
                }
            }
        }
        return byId.values.toList()
    }

    private fun mergeFields(
        a: MovieSubscriptionPlan,
        b: MovieSubscriptionPlan,
    ): MovieSubscriptionPlan {
        val perceived = when {
            a.perceivedPrice != null && b.perceivedPrice == null -> a.perceivedPrice
            b.perceivedPrice != null && a.perceivedPrice == null -> b.perceivedPrice
            a.perceivedPrice != null && b.perceivedPrice != null ->
                maxOf(a.perceivedPrice, b.perceivedPrice)
            else -> null
        }
        return MovieSubscriptionPlan(
            id = a.id,
            name = b.name?.takeIf { it.isNotBlank() } ?: a.name,
            price = if (b.price > 0) b.price else a.price,
            perceivedPrice = perceived,
            currencySymbol = b.currencySymbol?.takeIf { it.isNotBlank() }
                ?: a.currencySymbol?.takeIf { it.isNotBlank() },
            intervalUnit = b.intervalUnit ?: a.intervalUnit,
            intervalCount = b.intervalCount ?: a.intervalCount,
            isActive = a.isActive || b.isActive,
        )
    }
}
