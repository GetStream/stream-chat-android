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
data class SearchPayload (
    @Json(name = "filter_conditions")
    val filterConditions: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "force_default_search")
    val forceDefaultSearch: kotlin.Boolean? = null,

    @Json(name = "force_sql_v2_backend")
    val forceSqlV2Backend: kotlin.Boolean? = null,

    @Json(name = "limit")
    val limit: kotlin.Int? = null,

    @Json(name = "next")
    val next: kotlin.String? = null,

    @Json(name = "offset")
    val offset: kotlin.Int? = null,

    @Json(name = "query")
    val query: kotlin.String? = null,

    @Json(name = "sort")
    val sort: kotlin.collections.List<io.getstream.chat.android.network.models.SortParamRequest>? = emptyList(),

    @Json(name = "message_filter_conditions")
    val messageFilterConditions: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "message_options")
    val messageOptions: io.getstream.chat.android.network.models.MessageOptions? = null
)
