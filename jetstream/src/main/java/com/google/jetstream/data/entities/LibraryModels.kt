package com.google.jetstream.data.entities

import com.google.jetstream.data.util.LibraryClickAction

/** A shelf row on the My Library page — mobile-viewer `MyLibraryScreen` parity. */
data class LibraryShelf(
    val id: LibraryShelfId,
    val title: String,
    val items: List<LibraryItem> = emptyList(),
    val total: Int = 0,
    val hasMore: Boolean = false,
    val offset: Int = 0,
)

data class LibraryItem(
    val movieId: String,
    val movie: Movie,
    val progressPercent: Int = 0,
    val episodeLabel: String? = null,
    val layout: LibraryCardLayout = LibraryCardLayout.Landscape,
    val vodAssetId: Int? = null,
    val initialTimeSeconds: Int = 0,
    val clickAction: LibraryClickAction = LibraryClickAction.OpenMoviePage,
)

enum class LibraryShelfId(val apiId: String) {
    ContinueWatching("continue_watching"),
    RateShare("rate_share"),
    Purchased("purchased"),
    ExpiredPurchases("expired_purchases"),
    Rented("rented"),
    Bookmarks("bookmarks"),
    RentedExpired("rented_expired"),
    ;

    companion object {
        fun fromApiId(id: String): LibraryShelfId? =
            entries.firstOrNull { it.apiId == id }
    }
}

enum class LibraryCardLayout {
    Landscape,
    Portrait,
}

data class MyLibraryPage(
    val shelves: List<LibraryShelf> = emptyList(),
    val hasActiveSubscription: Boolean = false,
) {
    val isEmpty: Boolean = shelves.all { it.items.isEmpty() }
}
