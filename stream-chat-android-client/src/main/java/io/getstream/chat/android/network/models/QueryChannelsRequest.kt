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
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.Json

/**
 *
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class QueryChannelsRequest(
    @Json(name = "limit")
    internal val limit: Int? = null,

    @Json(name = "member_limit")
    internal val memberLimit: Int? = null,

    @Json(name = "message_limit")
    internal val messageLimit: Int? = null,

    @Json(name = "offset")
    internal val offset: Int? = null,

    @Json(name = "predefined_filter")
    internal val predefinedFilter: String? = null,

    @Json(name = "presence")
    internal val presence: Boolean? = null,

    @Json(name = "state")
    internal val state: Boolean? = null,

    @Json(name = "watch")
    internal val watch: Boolean? = null,

    @Json(name = "member_custom_include")
    internal val memberCustomInclude: List<String>? = emptyList(),

    @Json(name = "sort")
    internal val sort: List<io.getstream.chat.android.network.models.SortParamRequest>? = emptyList(),

    @Json(name = "filter_conditions")
    internal val filterConditions: Map<String, Any?>? = emptyMap(),

    @Json(name = "filter_values")
    internal val filterValues: Map<String, Any?>? = emptyMap(),

    @Json(name = "sort_values")
    internal val sortValues: Map<String, Any?>? = emptyMap(),
)
