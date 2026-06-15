/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.getstream.chat.android.network.models

import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.*
import kotlin.io.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * 
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class VelocityFilterConfig (
    @Json(name = "advanced_filters")
    val advancedFilters: kotlin.Boolean,

    @Json(name = "cascading_actions")
    val cascadingActions: kotlin.Boolean,

    @Json(name = "cids_per_user")
    val cidsPerUser: kotlin.Int,

    @Json(name = "enabled")
    val enabled: kotlin.Boolean,

    @Json(name = "first_message_only")
    val firstMessageOnly: kotlin.Boolean,

    @Json(name = "rules")
    val rules: kotlin.collections.List<io.getstream.chat.android.network.models.VelocityFilterConfigRule> = emptyList(),

    @Json(name = "async")
    val async: kotlin.Boolean? = null
)
