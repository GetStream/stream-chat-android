/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
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
data class QueryChannelsRequest (
    @Json(name = "limit")
    val limit: kotlin.Int? = null,

    @Json(name = "member_limit")
    val memberLimit: kotlin.Int? = null,

    @Json(name = "message_limit")
    val messageLimit: kotlin.Int? = null,

    @Json(name = "offset")
    val offset: kotlin.Int? = null,

    @Json(name = "predefined_filter")
    val predefinedFilter: kotlin.String? = null,

    @Json(name = "presence")
    val presence: kotlin.Boolean? = null,

    @Json(name = "state")
    val state: kotlin.Boolean? = null,

    @Json(name = "watch")
    val watch: kotlin.Boolean? = null,

    @Json(name = "sort")
    val sort: kotlin.collections.List<io.getstream.chat.android.network.models.SortParamRequest>? = emptyList(),

    @Json(name = "filter_conditions")
    val filterConditions: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "filter_values")
    val filterValues: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "sort_values")
    val sortValues: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap()
)
