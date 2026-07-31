package com.google.jetstream.data.entities

data class CollectionSectionDetails(
    val id: String,
    val slug: String,
    val title: String,
    val subheading: String?,
    val movies: MovieList,
    val total: Int,
    val heroPosterUri: String?,
)
