package com.google.jetstream.data.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extracts image URLs from Brew artwork fields that may be either a plain string
 * or `{ "url": "..." }` / thumbnail arrays of objects (also-watched API shape).
 */
object BrewArtworkUrls {

    fun asUrl(element: JsonElement?): String? {
        if (element == null) return null
        return when (element) {
            is JsonPrimitive -> element.contentOrNull?.takeIf { it.isNotBlank() }
            is JsonObject -> element["url"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            else -> null
        }
    }

    fun firstUrl(element: JsonElement?): String? {
        if (element == null) return null
        asUrl(element)?.let { return it }
        if (element is JsonArray) {
            for (item in element) {
                asUrl(item)?.let { return it }
            }
        }
        return null
    }

    fun landscapeFromAppearance(appearance: JsonObject?): String? {
        if (appearance == null) return null
        return firstUrl(appearance["horizontal_thumbnails"])
            ?: asUrl(appearance["background_art"])
            ?: asUrl(appearance["poster"])
            ?: asUrl(appearance["vertical_background_art"])
            ?: firstUrl(appearance["vertical_thumbnails"])
    }

    fun anyFromAppearance(appearance: JsonObject?): String? =
        landscapeFromAppearance(appearance)
}
