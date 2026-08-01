package com.google.jetstream.data.remote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/** get-campaign returns `project_trailer` as either a string URL or a string array. */
object StringOrStringListSerializer : KSerializer<List<String>> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeSerializableValue(delegate)
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.takeIf { it.isString }?.content?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let { listOf(it) }
                ?: emptyList()
            is JsonArray -> element.mapNotNull { item ->
                item.jsonPrimitive.takeIf { it.isString }?.content?.trim()?.takeIf { it.isNotBlank() }
            }
            else -> emptyList()
        }
    }

    override fun serialize(encoder: Encoder, value: List<String>) {
        delegate.serialize(encoder, value)
    }
}

fun List<String>.firstHttpTrailer(): String? =
    firstOrNull { it.startsWith("http") && !it.contains("youtube", ignoreCase = true) }

/**
 * get-campaign `pricing_data.buy` / `pricing_data.rent` are pricing option arrays when
 * purchasable, but `{ message, available }` objects when excluded from the viewer's region.
 */
object PricingOptionListSerializer : KSerializer<List<BrewPricingOptionDto>> {
    private val delegate = ListSerializer(BrewPricingOptionDto.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): List<BrewPricingOptionDto> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeSerializableValue(delegate)
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> jsonDecoder.json.decodeFromJsonElement(delegate, element)
            is JsonObject -> emptyList()
            else -> emptyList()
        }
    }

    override fun serialize(encoder: Encoder, value: List<BrewPricingOptionDto>) {
        delegate.serialize(encoder, value)
    }
}
