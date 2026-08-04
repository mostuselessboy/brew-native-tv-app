package com.google.jetstream.data.util

import com.google.jetstream.data.auth.BrewUser

private const val BrewBaseUrl = "https://www.brew.tv"
public const val DefaultCdnAvatarUrl = "https://createstir.b-cdn.net/stir-static/brew-avatars/brew-avatar-001.png"

/** Resolves avatar URL from user fields — parity with mobile `resolveUserAvatarUrl`. */
fun resolveUserAvatarUrl(user: BrewUser?): String {
    if (user == null) return DefaultCdnAvatarUrl
    val candidates = listOfNotNull(
        user.picture?.trim()?.takeIf { it.isNotBlank() },
        user.avatarUrl?.trim()?.takeIf { it.isNotBlank() },
        user.profileImageUrl?.trim()?.takeIf { it.isNotBlank() },
    )
    val first = candidates.firstOrNull() ?: DefaultCdnAvatarUrl
    return if (first.startsWith("http://", ignoreCase = true) ||
        first.startsWith("https://", ignoreCase = true)
    ) {
        first
    } else {
        "$BrewBaseUrl/${first.trimStart('/')}"
    }
}

/** Sized avatar for Coil — scales Brew CDN assets when possible. */
fun resolveUserAvatarDisplayUrl(user: BrewUser?, displaySizePx: Int): String {
    val raw = resolveUserAvatarUrl(user)
    return BrewImageUrl.withDimensions(raw, displaySizePx, displaySizePx, quality = "100")
}
