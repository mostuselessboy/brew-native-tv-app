package com.google.jetstream.data.util

import com.google.jetstream.data.auth.BrewUser

private const val BrewBaseUrl = "https://www.brew.tv"

/** Resolves avatar URL from user fields — parity with mobile `resolveUserAvatarUrl`. */
fun resolveUserAvatarUrl(user: BrewUser?): String? {
    if (user == null) return null
    val candidates = listOfNotNull(
        user.picture?.trim()?.takeIf { it.isNotBlank() },
        user.avatarUrl?.trim()?.takeIf { it.isNotBlank() },
        user.profileImageUrl?.trim()?.takeIf { it.isNotBlank() },
    )
    val first = candidates.firstOrNull() ?: return null
    return if (first.startsWith("http://", ignoreCase = true) ||
        first.startsWith("https://", ignoreCase = true)
    ) {
        first
    } else {
        "$BrewBaseUrl/${first.trimStart('/')}"
    }
}

/** Sized avatar for Coil — scales Brew CDN assets when possible. */
fun resolveUserAvatarDisplayUrl(user: BrewUser?, displaySizePx: Int): String? {
    val raw = resolveUserAvatarUrl(user) ?: return null
    return BrewImageUrl.withDimensions(raw, displaySizePx, displaySizePx, quality = "100")
}
