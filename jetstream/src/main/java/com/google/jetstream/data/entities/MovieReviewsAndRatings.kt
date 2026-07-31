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

package com.google.jetstream.data.entities

data class MovieReviewsAndRatings(
    val id: String = "",
    val reviewerName: String,
    val reviewerUsername: String = "",
    val reviewerIconUri: String,
    /** Review title / heading. */
    val reviewHeading: String = "",
    /** Review body text. */
    val reviewBody: String,
    /** Star rating on 0–5 scale for display. */
    val reviewRating: Double? = null,
    val createdAt: String = "",
    val countryCode: String = "",
    val countryName: String = "",
    val isVerifiedCritic: Boolean = false,
    val section: ReviewSection = ReviewSection.OtherCountries,
)
